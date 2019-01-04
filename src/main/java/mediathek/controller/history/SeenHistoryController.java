package mediathek.controller.history;

import mediathek.config.Daten;
import mediathek.gui.messages.history.DownloadHistoryChangedEvent;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SeenHistoryController extends MVUsedUrls<DownloadHistoryChangedEvent> {
    public SeenHistoryController() {
        super("history.txt", Daten.getSettingsDirectory_String(), DownloadHistoryChangedEvent.class);
    }

     /**
     * Check if URL string has already been seen.
     * This normally checks if the URL is contained in our history list.
     * A special case are Akamai´s CDN URLs:
     * Serveral subdomains (and most of the time they are separate servers) provide the same movie.
     * For CDN we check if the encodedPath to the movie is in our history
     *
     * @param strUrl the URL as string
     * @return true if URL was already seen, false otherwise
     */
    public boolean checkCdnDuplicate(String strUrl) {
        final HttpUrl url = HttpUrl.parse(strUrl);
        if (url == null)
            return false;

        final String privDom = url.topPrivateDomain();
        if (privDom != null) {
            if (!privDom.contains("akamaihd.net")) {
                //normale Prüfung
                return listeUrls.contains(url);
            } else {
                //hier müssen die Akamai CDN detailliert durchsucht werden ob die Dateien gleich sind
                //deutlich mehr aufwand...
                return checkSeenForCDN(url, strUrl);
            }

        } else
            return listeUrls.contains(url);
    }

    private boolean checkSeenForCDN(@NotNull final HttpUrl filmUrl, @NotNull final String strUrl) {
        boolean found = false;
        final String encodedPath = filmUrl.encodedPath();

        List<HttpUrl> urlList;
        synchronized (listeUrls) {
            urlList = listeUrls.parallelStream()
                    .filter(u -> strUrl.contains(u.encodedPath()))
                    .collect(Collectors.toList());
        }

        for (var url : urlList) {
            if (url.encodedPath().contains(encodedPath))
                found = true;
        }

        urlList.clear();
        return found;
    }
}
