import javax.swing.*;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run(){
                // display our weather app gui
                new WeatherAppGui().setVisible(true);

                //     System.out.println(weatherApp.getLocationData("Oslo"));

                //     System.out.println(weatherApp.getCurrentTime());
            }
        });
    }
}
