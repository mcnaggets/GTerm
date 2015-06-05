import com.google.common.base.Charsets;
import com.google.common.base.Stopwatch;
import com.google.common.io.Resources;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class Application {

    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

    private static Set<String> entries = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void main(String[] args) throws URISyntaxException, IOException {
        final Stopwatch stopwatch = Stopwatch.createStarted();

        Resources.readLines(Resources.getResource("Gene_Go.txt"), Charsets.UTF_8)
                .parallelStream().map(s -> s.split("\\s")[1]).forEach(Application::processId);

        writeToFile();

        System.out.println("Execution time: " + stopwatch.elapsed(TimeUnit.SECONDS) + " seconds");
    }

    private static void writeToFile() throws IOException {
        Path path = Paths.get(String.format("%s/result_%s.csv", System.getProperty("user.dir"), System.currentTimeMillis()));
        Files.deleteIfExists(path);
        Files.write(path, entries, CREATE_NEW);
    }

    private static void processId(String parent) {
        try {
            String url = String.format("http://www.ebi.ac.uk/QuickGO/GTerm?id=%s&format=mini", parent);
            Document document = Jsoup.connect(url).userAgent(USER_AGENT).get();
            document.select("table a[href]").parallelStream().filter(e -> e.attr("href").startsWith("GTerm"))
                    .map(e -> e.attr("href").substring(9))
                    .forEach(child -> {
                        final String entry = parent + "," + child;
                        if (!entries.contains(entry)) {
                            entries.add(entry);
                            processId(child);
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
