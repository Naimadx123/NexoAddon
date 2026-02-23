package zone.vao.thirdparties.updatechecker;

import com.tcoded.folialib.FoliaLib;
import com.tcoded.folialib.impl.PlatformScheduler;
import com.tcoded.folialib.wrapper.task.WrappedTask;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class UpdateChecker {

    static final String VERSION = "3.0.4";
    private static final String SPIGOT_CHANGELOG_SUFFIX = "/history";
    private static final String SPIGOT_DOWNLOAD_LINK = "https://www.spigotmc.org/resources/";
    private static final String SPIGOT_UPDATE_API = "https://api.spigotmc.org/simple/0.2/index.php?action=getResource&id=%s";
    private static final String POLYMART_CHANGELOG_SUFFIX = "/updates";
    private static final String POLYMART_DOWNLOAD_LINK = "https://polymart.org/resource/";
    private static final String POLYMART_UPDATE_API = "https://api.polymart.org/v1/getResourceInfoSimple/?resource_id=%s&key=version";
    private static final String SPIGET_UPDATE_API = "https://api.spiget.org/v2/resources/%s/versions/latest";
    private static final String GITHUB_RELEASE_API = "https://api.github.com/repos/%s/%s/releases";
    private static final String HANGAR_RELEASE_API =  "https://hangar.papermc.io/api/v1/projects/%s/%s/latest?channel=%s";
    private static UpdateChecker instance = null;
    private static boolean listenerAlreadyRegistered = false;

    static {
        checkRelocation();
    }

    @Getter
    private final String spigotUserId = "%%__USER__%%";
    private final String apiLink;
    private final ThrowingFunction<BufferedReader, String, IOException> mapper;
    private final UpdateCheckSource updateCheckSource;
    private final VersionSupplier supplier;
    private final Plugin plugin;

  @Getter
  private String changelogLink = null;
    @Getter
    private boolean checkedAtLeastOnce = false;

  @Getter
  private boolean coloredConsoleOutput = false;

  @Getter
  private String donationLink = null;
    private String freeDownloadLink = null;

  @Getter
  private String latestVersion = null;

  @Getter
  private String nameFreeVersion = "Free";
  @Getter
  private String namePaidVersion = "Paid";

  @Getter
  private boolean notifyOpsOnJoin = true;
    private String notifyPermission = null;

  @Getter
  private boolean notifyRequesters = true;
    private String supportLink = null;

  @Getter
  private boolean suppressUpToDateMessage = true;

  @Getter
  private BiConsumer<CommandSender[], Exception> onFail = (requesters, ex) -> ex.printStackTrace();

  @Getter
  private BiConsumer<CommandSender[], String> onSuccess = (requesters, latestVersion) -> {
    };
    private String paidDownloadLink = null;
    @Getter
    private static PlatformScheduler scheduler;
    @Nullable
    private WrappedTask updaterTask = null;
    private int timeout = 0;

  @Getter
  private String usedVersion;
    private String userAgentString = null;

  @Getter
  private boolean usingPaidVersion = false;

    {
        instance = this;
    }

    public UpdateChecker(@NotNull JavaPlugin plugin, @NotNull VersionSupplier supplier) {
        this.plugin = plugin;
        this.apiLink = null;
        this.supplier = supplier;
        this.updateCheckSource = null;
        this.mapper = null;
        init();
    }

    private void init() {
        Objects.requireNonNull(plugin, "Plugin cannot be null.");

        this.usedVersion = plugin.getDescription().getVersion().trim();

        if (detectPaidVersion()) {
            usingPaidVersion = true;
        }

        scheduler = new FoliaLib(plugin).getScheduler();

        if (!listenerAlreadyRegistered) {
            Bukkit.getPluginManager().registerEvents(new UpdateCheckListener(), plugin);
            listenerAlreadyRegistered = true;
        }
    }

    private boolean detectPaidVersion() {
        return spigotUserId.matches("^[0-9]+$");
    }

      public UpdateChecker(@NotNull JavaPlugin plugin, @NotNull UpdateCheckSource updateCheckSource, @NotNull String parameter) {

        this.plugin = plugin;

        this.supplier = null;

        final String apiLink;
        final ThrowingFunction<BufferedReader, String, IOException> mapper;

        switch (this.updateCheckSource = updateCheckSource) {
            case CUSTOM_URL: {
                apiLink = parameter;
                mapper = VersionMapper.TRIM_FIRST_LINE;
                break;
            }
            case SPIGOT: {
                apiLink = String.format(SPIGOT_UPDATE_API, parameter);
                mapper = VersionMapper.SPIGOT;
                break;
            }
            case POLYMART: {
                apiLink = String.format(POLYMART_UPDATE_API, parameter);
                mapper = VersionMapper.TRIM_FIRST_LINE;
                break;
            }
            case SPIGET: {
                apiLink = String.format(SPIGET_UPDATE_API, parameter);
                mapper = VersionMapper.SPIGET;
                break;
            }
            case GITHUB_RELEASE_TAG: {
                String[] split = parameter.split("/");
                if (split.length < 2) {
                    throw new IllegalArgumentException("Given GitHub repository must be in the format \"<UserOrOrganizationName>/<RepositoryName>\"");
                }
                apiLink = String.format(GITHUB_RELEASE_API, split[0], split[1]);
                mapper = VersionMapper.GITHUB_RELEASE_TAG;
                break;
            }
            case HANGAR: {
                String[] split = parameter.split("/");
                if (split.length <3) {
                    throw new IllegalArgumentException("Given HangarMC project must be in the format \"<UserOrOrganizationName>/<ProjectName>/<ReleaseChannel>\"");
                }

                apiLink = String.format(HANGAR_RELEASE_API, split[0], split[1], split[2]);
                mapper = VersionMapper.TRIM_FIRST_LINE;
                break;
            }
            default:
                throw new UnsupportedOperationException();
        }

        Objects.requireNonNull(apiLink, "API Link cannot be null.");

        this.apiLink = apiLink;
        this.mapper = mapper;

        init();

    }

       private static void checkRelocation() {
        if (Bukkit.getServer().getClass().getName().equals("be.seeseemelk.mockbukkit.ServerMock")) return;
        final String defaultPackageDe = new String(new byte[]{'d', 'e', '.', 'j', 'e', 'f', 'f', '_', 'm', 'e', 'd', 'i', 'a', '.', 'u', 'p', 'd', 'a', 't', 'e', 'c', 'h', 'e', 'c', 'k', 'e', 'r'});
        final String defaultPackageCom = new String(new byte[]{'c', 'o', 'm', '.', 'j', 'e', 'f', 'f', '_', 'm', 'e', 'd', 'i', 'a', '.', 'u', 'p', 'd', 'a', 't', 'e', 'c', 'h', 'e', 'c', 'k', 'e', 'r'});
        final String examplePackage = new String(new byte[]{'y', 'o', 'u', 'r', '.', 'p', 'a', 'c', 'k', 'a', 'g', 'e'});
        String packageName = UpdateChecker.class.getPackage().getName();
        if (packageName.startsWith(defaultPackageDe) || packageName.startsWith(defaultPackageCom) || packageName.startsWith(examplePackage)) {
            throw new IllegalStateException("SpigotUpdateChecker class has not been relocated correctly! Check the GitHub's README.md for instructions.");
        }
    }

  public UpdateChecker checkEveryXHours(double hours) {
        double minutes = hours * 60;
        double seconds = minutes * 60;
        long ticks = ((int) seconds) * 20L;
        stop();
        if (ticks > 0) {
            updaterTask = getScheduler().runTimer(() -> checkNow(Bukkit.getConsoleSender()), ticks, ticks);
        } else {
            updaterTask = null;
        }
        return this;
    }

    public UpdateChecker stop() {
        if (updaterTask != null) {
            updaterTask.cancel();
        }
        updaterTask = null;
        return this;
    }

    public UpdateChecker checkNow(@Nullable CommandSender... requesters) {
        if (plugin == null) {
            throw new IllegalStateException("Plugin has not been set.");
        }
        if (apiLink == null && supplier == null) {
            throw new IllegalStateException("API Link has not been set and no supplier was provided.");
        }

        checkedAtLeastOnce = true;

        if (userAgentString == null) {
            userAgentString = UserAgentBuilder.getDefaultUserAgent().build();
        }

        getScheduler().runAsync(taskAsync -> {

            UpdateCheckEvent updateCheckEvent;

            try {
                if (supplier != null) {
                    latestVersion = supplier.getLatestVersionString();
                } else {
                    final HttpURLConnection httpConnection = (HttpURLConnection) new URL(apiLink).openConnection();
                    httpConnection.addRequestProperty("User-Agent", userAgentString);
                    if (timeout > 0) {
                        httpConnection.setConnectTimeout(timeout);
                    }
                    try (final InputStreamReader input = new InputStreamReader(httpConnection.getInputStream()); final BufferedReader reader = new BufferedReader(input)) {
                        latestVersion = mapper.apply(reader);
                    }
                }

                if (!isUsingLatestVersion() && !isOtherVersionNewer(usedVersion, latestVersion)) {
                    latestVersion = usedVersion;
                }

                updateCheckEvent = new UpdateCheckEvent(UpdateCheckSuccess.SUCCESS);
            } catch (final IOException exception) {
                updateCheckEvent = new UpdateCheckEvent(UpdateCheckSuccess.FAIL);
                getScheduler().runNextTick(task -> getOnFail().accept(requesters, exception));
            }

            UpdateCheckEvent finalUpdateCheckEvent = updateCheckEvent.setRequesters(requesters);

            getScheduler().runNextTick(task -> {

                if (finalUpdateCheckEvent.getSuccess() == UpdateCheckSuccess.SUCCESS) {
                    getOnSuccess().accept(requesters, latestVersion);
                }

                Bukkit.getPluginManager().callEvent(finalUpdateCheckEvent);
            });

        });
        return this;
    }

    public boolean isUsingLatestVersion() {
        return usedVersion.equals(instance.latestVersion);
    }

    public static boolean isOtherVersionNewer(String myVersion, String otherVersion) {
        DefaultArtifactVersion used = new DefaultArtifactVersion(myVersion);
        DefaultArtifactVersion latest = new DefaultArtifactVersion(otherVersion);
        return used.compareTo(latest) < 0;
    }

    public UpdateChecker checkNow() {
        checkNow(Bukkit.getConsoleSender());
        return this;
    }

    public List<String> getAppropriateDownloadLinks() {
        List<String> list = new ArrayList<>();

        if (usingPaidVersion) {
            if (paidDownloadLink != null) {
                list.add(paidDownloadLink);
            } else if (freeDownloadLink != null) {
                list.add(freeDownloadLink);
            }
        } else {
            if (paidDownloadLink != null) {
                list.add(paidDownloadLink);
            }
            if (freeDownloadLink != null) {
                list.add(freeDownloadLink);
            }
        }
        return list;
    }

    public UpdateChecker setChangelogLink(int resourceId) {
        if (updateCheckSource == UpdateCheckSource.SPIGOT)
            return setChangelogLink(SPIGOT_DOWNLOAD_LINK + resourceId + SPIGOT_CHANGELOG_SUFFIX);
        if (updateCheckSource == UpdateCheckSource.POLYMART)
            return setChangelogLink(POLYMART_DOWNLOAD_LINK + resourceId + POLYMART_CHANGELOG_SUFFIX);
        return this;
    }
    public UpdateChecker setChangelogLink(@Nullable String link) {
        changelogLink = link;
        return this;
    }

    @Nullable
    public String getSupportLink() {
        return supportLink;
    }

    @NotNull
    public UpdateChecker setSupportLink(@Nullable String link) {
        this.supportLink = link;
        return this;
    }

    public UpdateChecker setDonationLink(@Nullable String donationLink) {
        this.donationLink = donationLink;
        return this;
    }

    public UpdateCheckResult getLastCheckResult() {
        if (latestVersion == null) {
            return UpdateCheckResult.UNKNOWN;
        }
        if (latestVersion.equals(usedVersion)) {
            return UpdateCheckResult.RUNNING_LATEST_VERSION;
        }
        return UpdateCheckResult.NEW_VERSION_AVAILABLE;
    }

    public UpdateChecker setNameFreeVersion(String nameFreeVersion) {
        this.nameFreeVersion = nameFreeVersion;
        return this;
    }

    public UpdateChecker setNamePaidVersion(String namePaidVersion) {
        this.namePaidVersion = namePaidVersion;
        return this;
    }

    public @Nullable
    String getNotifyPermission() {
        return notifyPermission;
    }

    protected Plugin getPlugin() {
        return plugin;
    }

     public UpdateChecker setUsedVersion(String usedVersion) {
        this.usedVersion = usedVersion;
        return this;
    }

  public UpdateChecker setColoredConsoleOutput(boolean coloredConsoleOutput) {
        this.coloredConsoleOutput = coloredConsoleOutput;
        return this;
    }

    public UpdateChecker setNotifyOpsOnJoin(boolean notifyOpsOnJoin) {
        this.notifyOpsOnJoin = notifyOpsOnJoin;
        return this;
    }

     public UpdateChecker setNotifyRequesters(boolean notify) {
        notifyRequesters = notify;
        return this;
    }

     public UpdateChecker setUsingPaidVersion(boolean paidVersion) {
        usingPaidVersion = paidVersion;
        return this;
    }

    public UpdateChecker onFail(BiConsumer<CommandSender[], Exception> onFail) {
        this.onFail = onFail == null ? (requesters, ex) -> ex.printStackTrace() : onFail;
        return this;
    }

    public UpdateChecker onSuccess(BiConsumer<CommandSender[], String> onSuccess) {
        this.onSuccess = onSuccess == null ? (requesters, latestVersion) -> {
        } : onSuccess;
        return this;
    }

    public UpdateChecker setDownloadLink(int resourceId) {
        if (updateCheckSource == UpdateCheckSource.SPIGOT) return setDownloadLink(SPIGOT_DOWNLOAD_LINK + resourceId);
        if (updateCheckSource == UpdateCheckSource.POLYMART)
            return setDownloadLink(POLYMART_DOWNLOAD_LINK + resourceId);
        return this;
    }

    public UpdateChecker setDownloadLink(@Nullable String downloadLink) {
        this.paidDownloadLink = null;
        this.freeDownloadLink = downloadLink;
        return this;
    }

    public UpdateChecker suppressUpToDateMessage(boolean suppress) {
        this.suppressUpToDateMessage = suppress;
        return this;
    }

    public UpdateChecker setFreeDownloadLink(int resourceId) {
        if (updateCheckSource == UpdateCheckSource.SPIGOT)
            return setFreeDownloadLink(SPIGOT_DOWNLOAD_LINK + resourceId);
        if (updateCheckSource == UpdateCheckSource.POLYMART)
            return setFreeDownloadLink(POLYMART_DOWNLOAD_LINK + resourceId);
        return this;
    }

    public UpdateChecker setFreeDownloadLink(@Nullable String freeDownloadLink) {
        this.freeDownloadLink = freeDownloadLink;
        return this;
    }

    public UpdateChecker setNotifyByPermissionOnJoin(@Nullable String permission) {
        notifyPermission = permission;
        return this;
    }

    public UpdateChecker setPaidDownloadLink(int resourceId) {
        if (updateCheckSource == UpdateCheckSource.SPIGOT)
            return setPaidDownloadLink(SPIGOT_DOWNLOAD_LINK + resourceId);
        if (updateCheckSource == UpdateCheckSource.POLYMART)
            return setPaidDownloadLink(POLYMART_DOWNLOAD_LINK + resourceId);
        return this;
    }

    public UpdateChecker setPaidDownloadLink(@NotNull String link) {
        paidDownloadLink = link;
        return this;
    }

    public UpdateChecker setTimeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public UpdateChecker setUserAgent(@NotNull UserAgentBuilder userAgentBuilder) {
        userAgentString = userAgentBuilder.build();
        return this;
    }

    public UpdateChecker setUserAgent(@Nullable String userAgent) {
        userAgentString = userAgent;
        return this;
    }

}
