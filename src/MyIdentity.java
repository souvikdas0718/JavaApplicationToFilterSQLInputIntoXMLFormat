import java.util.Properties;

/*
 * @desc This Class establishes helps in establishing jdbc connectivity by fetching user credentials.
 * @method setIdentity sets the identity of users
 * @referred from LAB 6 assignment
 * @author souvikdas
 */
public class MyIdentity {

    public static void setIdentity(Properties properties) {
        //Referred Lab 5 class file
        properties.setProperty("database", "csci3901");
        properties.setProperty("user", "souvik");  // Replacing my CSID for bluenose
        properties.setProperty("password", "B00847127"); // Replacing my BannerID with my SQL password
    }
}