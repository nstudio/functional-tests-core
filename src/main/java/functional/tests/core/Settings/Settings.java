package functional.tests.core.Settings;

import java.io.*;
import java.util.Locale;
import java.util.Properties;

import functional.tests.core.Enums.DeviceType;
import functional.tests.core.Enums.OSType;
import functional.tests.core.Enums.PlatformType;
import functional.tests.core.Exceptions.UnknownDeviceTypeException;
import functional.tests.core.Exceptions.UnknownOSException;
import functional.tests.core.Exceptions.UnknownPlatformException;
import functional.tests.core.Log.Log;
import io.appium.java_client.remote.AutomationName;
import org.apache.commons.io.FileUtils;

public class Settings {

    private static final String userDir = System.getProperty("user.dir");
    private static final String appConfigPath = System.getProperty("appConfig");
    private static final String baseResourcesDir = userDir + File.separator + "resources";
    private static final String baseOutputDir = userDir + File.separator + "target" + File.separator + "surefire-reports";

    private static Properties properties;

    public static OSType OS;
    public static PlatformType platform;
    public static DeviceType deviceType;
    public static boolean isRealDevice;
    public static int defaultTimeout;
    public static int deviceBootTimeout;
    public static String platformVersion;
    public static String deviceId;
    public static String deviceName;
    public static String testAppName;
    public static String testAppArchive;
    public static String appiumVersion;
    public static String automationName;
    public static String emulatorOptions;
    public static String emulatorCreateOptions;
    public static String simulatorType;
    public static String baseLogDir;
    public static String screenshotOutDir;
    public static String screenshotResDir;
    public static String appiumLogFile;
    public static final String baseTestAppDir = userDir + File.separator + "testapp";

    private static OSType getOSType() {
        OSType detectedOS;

        String OS = System.getProperty("os.name", "generic").toLowerCase();
        if ((OS.contains("mac")) || (OS.contains("darwin"))) {
            detectedOS = OSType.MacOS;
        } else if (OS.contains("win")) {
            detectedOS = OSType.Windows;
        } else if (OS.contains("nux")) {
            detectedOS = OSType.Linux;
        } else {
            detectedOS = OSType.Other;
        }

        return detectedOS;
    }

    private static void setupLocations() throws IOException {
        baseLogDir = baseOutputDir + File.separator + "logs";
        screenshotOutDir = baseOutputDir + File.separator + "screenshots";
        screenshotResDir = baseResourcesDir + File.separator + "images";
        appiumLogFile = baseLogDir + File.separator + "appium.log";

        try {
            File baseScreenshotDirLocation = new File(screenshotOutDir);
            baseScreenshotDirLocation.mkdirs();
            FileUtils.cleanDirectory(baseScreenshotDirLocation);
        } catch (IOException e) {
            Log.fatal("Failed to cleanup and create screenshot output folder.");
            throw new IOException(e);
        }
    }

    private static Properties readProperties() throws Exception {
        String appConfigFile = userDir + File.separator + appConfigPath;
        try {
            InputStream input = new FileInputStream(appConfigFile);
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        } catch (Exception e) {
            Log.fatal("Failed to read and init settings. Please check if " + appConfigFile + " exists.");
            throw new Exception(e);
        }
    }

    private static PlatformType getPlatformType() {
        String platformTypeString = properties.getProperty("platformName");
        if (platformTypeString.equalsIgnoreCase("Android")) {
            return PlatformType.Andorid;
        } else if (platformTypeString.equalsIgnoreCase("iOS")) {
            return PlatformType.iOS;
        } else {
            return PlatformType.Other;
        }
    }

    private static DeviceType getDeviceType() {
        String deviceTypeString = properties.getProperty("deviceType");
        if (deviceTypeString.equalsIgnoreCase("android")) {
            return DeviceType.Android;
        } else if (deviceTypeString.equalsIgnoreCase("ios")) {
            return DeviceType.iOS;
        } else if (deviceTypeString.toLowerCase().contains("emu")) {
            return DeviceType.Emulator;
        } else if (deviceTypeString.toLowerCase().contains("sim")) {
            return DeviceType.Simulator;
        } else {
            return DeviceType.Other;
        }
    }

    private static void verifyTestAppPath() throws FileNotFoundException {
        File appDir = new File(baseTestAppDir);
        File app = new File(appDir, Settings.testAppName);

        if (deviceType == DeviceType.Simulator) {
            app = new File(appDir, Settings.testAppArchive);
        }

        if (!app.exists()) {
            String message = "Failed to find test app: " + app.getAbsolutePath();
            Log.fatal(message);
            throw new FileNotFoundException(message);
        }
    }

    public static void initSettings() throws Exception {
        // Set locations and cleanup output folders
        setupLocations();

        // Get current OS and verify it
        OS = getOSType();
        if ((OS == OSType.Other)) {
            Log.fatal("Unknown OS.");
            throw new UnknownOSException("Unknown OS.");
        }

        properties = readProperties();

        // Get mobile platform and verify it
        platform = getPlatformType();
        if ((platform == PlatformType.Other)) {
            Log.fatal("Unknown mobile platform.");
            throw new UnknownPlatformException("Unknown mobile platform.");
        }

        // Get device type and verify it
        deviceType = getDeviceType();
        if ((deviceType == DeviceType.Other)) {
            Log.fatal("Unknown device platform.");
            throw new UnknownDeviceTypeException("Unknown device type.");
        }

        // Set isRealDevice
        if ((deviceType == DeviceType.Simulator) || (deviceType == DeviceType.Emulator)) {
            isRealDevice = false;
        } else {
            isRealDevice = true;
        }

        deviceId = properties.getProperty("udid");
        deviceName = properties.getProperty("deviceName");
        platformVersion = properties.getProperty("platformVersion");
        testAppName = properties.getProperty("testAppName");
        testAppArchive = properties.getProperty("testAppArchive");
        appiumVersion = properties.getProperty("appiumVersion");
        emulatorOptions = properties.getProperty("emulatorOptions");
        emulatorCreateOptions = properties.getProperty("emulatorCreateOptions");
        simulatorType = properties.getProperty("simulatorType");
        appiumVersion = properties.getProperty("appiumVersion");

        // Set automation name
        String automationNameString = properties.getProperty("automationName");
        if ((automationNameString != null) && (automationNameString.equalsIgnoreCase("selendroid"))) {
            automationName = AutomationName.SELENDROID;
        } else {
            automationName = AutomationName.APPIUM;
        }

        // If defaultTimeout is not specified set it to 30 sec.
        String defaultTimeoutString = properties.getProperty("defaultTimeout");
        if (defaultTimeoutString != null) {
            defaultTimeout = Integer.valueOf(defaultTimeoutString);
        } else {
            defaultTimeout = 30;
        }

        // If deviceBootTimeout is not specified set it equal to defaultTimeout
        String deviceBootTimeoutString = properties.getProperty("deviceBootTimeout");
        if (deviceBootTimeoutString != null) {
            deviceBootTimeout = Integer.valueOf(deviceBootTimeoutString);
        } else {
            deviceBootTimeout = defaultTimeout;
        }

        // Verify app under tests exists
        verifyTestAppPath();

        // Verify OS and Mobile Platform
        if ((platform == PlatformType.iOS) && (OS != OSType.MacOS)) {
            String error = "Can not run iOS tests on Windows and Linux";
            Log.fatal(error);
            throw new Exception(error);
        }

        Log.info("=============================================");
        Log.info("Settings  initialized properly:");
        Log.info("OS Type: " + OS);
        Log.info("Mobile Platoform: " + platform);
        Log.info("Platform Version: " + platformVersion);
        Log.info("Device Type: " + deviceType);
        Log.info("Device Name: " + deviceName);
        Log.info("Real Device: " + isRealDevice);
        Log.info("Device Id: " + deviceId);
        Log.info("Default Timeout: " + defaultTimeout);
        Log.info("Device Boot Time: " + deviceBootTimeout);
        Log.info("Base TestApp Path: " + baseTestAppDir);
        Log.info("TestApp Name: " + testAppName);
        Log.info("TestApp Archive: " + testAppArchive);
        Log.info("Appium Version: " + appiumVersion);
        Log.info("Automation Name: " + automationName);
        Log.info("Emulator Options: " + emulatorOptions);
        Log.info("Emulator Create Options: " + emulatorCreateOptions);
        Log.info("Simulator Type: " + simulatorType);
        Log.info("Log Output Folder: " + baseLogDir);
        Log.info("Screenshot Output Folder: " + screenshotOutDir);
        Log.info("Screenshot Resources Folder: " + screenshotResDir);
        Log.info("Appium Log File: " + appiumLogFile);
        Log.info("=============================================");
        Log.info("");
    }
}