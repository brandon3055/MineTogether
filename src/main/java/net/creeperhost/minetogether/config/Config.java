package net.creeperhost.minetogether.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;

/**
 * Created by Aaron on 05/05/2017.
 */
public class Config
{
    private transient String version;
    public String curseProjectID;
    private String promoCode;
    private boolean creeperhostEnabled;
    private boolean mpMenuEnabled;
    private boolean mainMenuEnabled;
    private boolean serverHostButtonImage;
    private boolean serverHostMenuImage;
    private boolean sivIntegration;
    private boolean serverListEnabled;
    private boolean chatEnabled;
    private boolean autoMT;
    private boolean enableFriendOnlineToasts;
    private boolean enableMainMenuFriends;
    private boolean replaceRealms;
    private String issueTrackerUrl;

    private int pregenDiameter = 120;
    
    public static Config instance;
    
    public Config()
    {
        this.version = "0";
        curseProjectID = "Insert curse project ID here";
        promoCode = "Insert Promo Code here";
        creeperhostEnabled = true;
        mpMenuEnabled = true;
        mainMenuEnabled = true;
        serverHostButtonImage = true;
        serverHostMenuImage = true;
        sivIntegration = true;
        serverListEnabled = true;
        chatEnabled = true;
        autoMT = true;
        enableFriendOnlineToasts = true;
        enableMainMenuFriends = true;
        replaceRealms = true;
        issueTrackerUrl = "https://pste.ch/";
    }
    
    private Config(String version, String promoCode, boolean creeperhostEnabled, boolean mpMenuEnabled, boolean mainMenuEnabled, boolean serverHostButtonImage, boolean serverHostMenuImage)
    {
        super();
        this.version = version;
        this.promoCode = promoCode;
        this.creeperhostEnabled = creeperhostEnabled;
        this.mpMenuEnabled = mpMenuEnabled;
        this.mainMenuEnabled = mainMenuEnabled;
        this.serverHostButtonImage = serverHostButtonImage;
        this.serverHostMenuImage = serverHostMenuImage;
    }
    
    public static Config getInstance()
    {
        return instance;
    }
    
    public String getVersion()
    {
        return version;
    }
    
    public void setVersion(String version)
    {
        this.version = version;
    }
    
    public String getPromo()
    {
        return promoCode;
    }
    
    public boolean isSivIntegration()
    {
        return sivIntegration;
    }
    
    public boolean isMpMenuEnabled()
    {
        return mpMenuEnabled;
    }
    
    public boolean isCreeperhostEnabled()
    {
        return creeperhostEnabled;
    }
    
    public boolean isMainMenuEnabled()
    {
        return mainMenuEnabled;
    }
    
    public boolean isServerHostButtonImage()
    {
        return serverHostButtonImage;
    }
    
    public boolean isServerHostMenuImage()
    {
        return serverHostMenuImage;
    }
    
    public boolean isServerListEnabled()
    {
        return serverListEnabled;
    }
    
    public int getPregenDiameter()
    {
        return pregenDiameter;
    }
    
    public void setMpMenuEnabled(boolean value)
    {
        mpMenuEnabled = value;
    }
    
    public void setServerListEnabled(boolean value)
    {
        serverListEnabled = value;
    }
    
    public void setChatEnabled(boolean value)
    {
        chatEnabled = value;
    }
    
    public void setEnableFriendOnlineToasts(boolean enableFriendOnlineToasts)
    {
        this.enableFriendOnlineToasts = enableFriendOnlineToasts;
    }
    
    public boolean isChatEnabled()
    {
        return chatEnabled;
    }
    
    public boolean isFriendOnlineToastsEnabled()
    {
        return enableFriendOnlineToasts;
    }
    
    public String getCurseProjectID()
    {
        return curseProjectID;
    }
    
    public boolean isAutoMT()
    {
        return autoMT;
    }
    
    public boolean isEnableMainMenuFriends()
    {
        return enableMainMenuFriends;
    }

    public boolean getReplaceRealms() { return replaceRealms; }

    public void setEnableMainMenuFriends(boolean enableMainMenuFriends)
    {
        this.enableMainMenuFriends = enableMainMenuFriends;
    }

    public String getIssueTrackerUrl()
    {
        return issueTrackerUrl;
    }

    public static void makeConfig(String version, String promoCode, boolean creeperhostEnabled, boolean mpMenuEnabled, boolean mainMenuEnabled, boolean serverHostButtonImage, boolean serverHostMenuImage)
    {
        if (instance != null)
        {
            return;
        }
        
        instance = new Config(version, promoCode, creeperhostEnabled, mpMenuEnabled, mainMenuEnabled, serverHostButtonImage, serverHostMenuImage);
    }
    
    public static void loadFromFile(File file)
    {
        Gson gson = new Gson();
        try
        {
            FileReader fileReader = new FileReader(file);
            instance = gson.fromJson(fileReader, Config.class);
        } catch (Exception ignored)
        {
        }
    }
    
    public static String saveConfig()
    {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(instance);
    }
}
