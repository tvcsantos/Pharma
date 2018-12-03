/*
 * PharmaApp.java
 */

package pt.com.santos.pharma;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.github.sarxos.webcam.util.jh.JHFlipFilter;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class PharmaApp extends SingleFrameApplication {

    public static final File SETTINGS_DIR =
            new File(AppUtils.USER_HOME, ".pharma");

    private Webcam webcam;
    private Connection dbConn;
    private Statement stat;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override protected void startup() {
        if (!SETTINGS_DIR.exists()) SETTINGS_DIR.mkdir();
        webcam = Webcam.getDefault();
        if (webcam != null) {
            webcam.setViewSize(WebcamResolution.VGA.getSize());
        }
        try {
            Class.forName("org.sqlite.JDBC");
            String sdir = SETTINGS_DIR.toString() + "/pharmadb.db";
            dbConn = DriverManager.getConnection("jdbc:sqlite:" + sdir);
            execUpdate("CREATE TABLE IF NOT EXISTS Pharma ("
                    + " BARCODE NUMERIC NOT NULL,"
                    + " NAME VARCHAR(200) NOT NULL,"
                    + " EXPIRATION DATE NOT NULL,"
                    + " UNITS INTEGER NOT NULL DEFAULT 1,"
                    + " PRIMARY KEY (BARCODE, EXPIRATION)"
                    + ");");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PharmaApp.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(PharmaApp.class.getName())
                    .log(Level.SEVERE, null, ex);
            dbConn = null;
        }
        
        show(new PharmaView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of PharmaApp
     */
    public static PharmaApp getApplication() {
        return Application.getInstance(PharmaApp.class);
    }

    private void execUpdate(String sql) throws SQLException {
        stat = dbConn.createStatement();
        stat.executeUpdate(sql);
        closeStat();
    }

    private ResultSet execQuery(String query) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet res = stat.executeQuery(query);
        return res;
    }

    public ResultSet list() throws SQLException {
        return execQuery("SELECT * FROM Pharma;");
    }

    public ResultSet expired() throws SQLException {
        Date today = Calendar.getInstance().getTime();
        today.setMinutes(0);
        today.setSeconds(0);
        today.setHours(0);
        return execQuery("SELECT * FROM Pharma " +
                "WHERE EXPIRATION <= " + today.getTime());
    }

    public int expiredSize() throws SQLException {
        Date today = Calendar.getInstance().getTime();
        today.setMinutes(0);
        today.setSeconds(0);
        today.setHours(0);
        ResultSet res = execQuery("SELECT SUM(UNITS) FROM (SELECT UNITS FROM Pharma " +
                "WHERE EXPIRATION <= " + today.getTime() + ")");
        res.next();
        int result = res.getInt(1);
        closeStat();
        return result;
    }

    public void closeStat() throws SQLException {
        if (stat != null) stat.close();
        stat = null;
    }

    public ResultSet select(long barcode) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet res = stat.executeQuery(
                "SELECT * FROM Pharma WHERE BARCODE=" + barcode + ";");
        return res;
    }

    public ResultSet select(long barcode, Date exp) throws SQLException {
        if (exp == null) return select(barcode);
        stat = dbConn.createStatement();
        ResultSet res = stat.executeQuery(
                "SELECT * FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        return res;
    }

    public boolean delete(long barcode, Date exp) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet rs = stat.executeQuery(
                "SELECT * FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        if (!rs.next()) {
            closeStat();
            return false;
        } else {
            int units = rs.getInt(4);
            closeStat();
             stat = dbConn.createStatement();
            if (units > 1) {
                stat.executeUpdate("UPDATE Pharma SET UNITS = UNITS - 1 " +
                    "WHERE BARCODE=" + barcode +
                    " and EXPIRATION=" + exp.getTime() + ";");
            } else {
                 stat.executeUpdate("DELETE FROM Pharma " +
                    "WHERE BARCODE=" + barcode +
                    " and EXPIRATION=" + exp.getTime() + ";");
            }
            closeStat();
            return true;
        }
    }

    public boolean hasCode(long barcode, Date exp) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet rs = stat.executeQuery(
                "SELECT COUNT(*) FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        rs.next();
        int count = rs.getInt(1);
        closeStat();
        if (count <= 0) return false;
        else return true;
    }

    public boolean update(long barcode, Date exp) throws SQLException {
        stat = dbConn.createStatement();
        ResultSet rs = stat.executeQuery(
                "SELECT COUNT(*) FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        rs.next();
        int count = rs.getInt(1);
        closeStat();
        if (count <= 0) { //must insert
            return false;
        } else {
            stat = dbConn.createStatement();
            stat.executeUpdate("UPDATE Pharma SET UNITS = UNITS + 1 " +
                    "WHERE BARCODE=" + barcode +
                    " and EXPIRATION=" + exp.getTime() + ";");
            closeStat();
            return true;
        }
    }

    public boolean update(long barcode, Date exp, int units)
            throws SQLException {
        stat = dbConn.createStatement();
        ResultSet rs = stat.executeQuery(
                "SELECT COUNT(*) FROM Pharma WHERE BARCODE=" + barcode +
                " and EXPIRATION=" + exp.getTime() + ";");
        rs.next();
        int count = rs.getInt(1);
        closeStat();
        if (count <= 0) { //must insert
            return false;
        } else {
            stat = dbConn.createStatement();
            stat.executeUpdate("UPDATE Pharma SET UNITS =" + units +
                    " WHERE BARCODE=" + barcode +
                    " and EXPIRATION=" + exp.getTime() + ";");
            closeStat();
            return true;
        }
    }

    public void insert(long barcode, String name, Date exp, int units)
            throws SQLException {
        stat = dbConn.createStatement();
        stat.executeUpdate("INSERT INTO Pharma " +
                "(BARCODE, NAME, EXPIRATION, UNITS)" +
            "VALUES(" + barcode + ",\"" + name + "\"" +
            "," + exp.getTime() + "," + units + ")");
	closeStat();
    }

    public void insert(long barcode, String name, Date exp)
            throws SQLException {
        stat = dbConn.createStatement();
        stat.executeUpdate("INSERT INTO Pharma " +
                "(BARCODE, NAME, EXPIRATION)" +
            "VALUES(" + barcode + ",\"" + name + "\"" +
            "," + exp.getTime() + ")");
	closeStat();
    }
    
    public boolean hasWebCam() {
        return webcam != null;
    }
    
    public Component getPlayerVisualComponent() throws IOException {
        if (webcam != null) {
            WebcamPanel panel = new WebcamPanel(webcam);
            //panel.setFPSDisplayed(true);
            //panel.setDisplayDebugInfo(true);
            //panel.setImageSizeDisplayed(true);    
            //panel.setMirrored(true);
            return panel;
        }
        return null;
    }

    public BufferedImage grabFrame() {
        return webcam.getImage();
    }

    public static String getDecodeText(BufferedImage image)
            throws InvalidParameterException, NotFoundException
    {
        if (image == null) {
            throw new InvalidParameterException("Image cannot be null");
        }
      
        for (int i = 0 ; i < 4; i++) {
            //saveImage(image, "test" + i + ".png");
            try {
                LuminanceSource source =
                        new BufferedImageLuminanceSource(image);
                BinaryBitmap bitmap =
                        new BinaryBitmap(new HybridBinarizer(source));
                Result result = new MultiFormatReader().decode(bitmap);
                return result.getText();
            } catch(NotFoundException ex) {
                image = rotate90(image);
                if (i == 3) throw ex;
                continue;
            }
        }
        return null;
    }
    
    private static BufferedImage rotate90(BufferedImage img) {
        final JHFlipFilter rotate = new JHFlipFilter(JHFlipFilter.FLIP_90CW);
        return rotate.filter(img, null);
    }
    
    /*
    private static void saveImage(BufferedImage img, String ref) {
        try {
            String format = (ref.endsWith(".png")) ? "png" : "jpg";
            ImageIO.write(img, format, new File(ref));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {
        java.util.logging.Logger logger = java.util.logging.Logger.getLogger(
                org.jdesktop.application.SessionStorage.class.getName());
        logger.setLevel(java.util.logging.Level.OFF);
        launch(PharmaApp.class, args);
    }    
}
