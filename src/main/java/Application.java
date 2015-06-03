import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

public class Application {

    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36";

    private static AtomicInteger count = new AtomicInteger();

    public static void main(String[] args) throws URISyntaxException, IOException {
        Path path = Paths.get(System.getProperty("user.dir") + "/result.csv");
        long time = System.currentTimeMillis();
        try (BufferedWriter writer = Files.newBufferedWriter(path, CREATE, APPEND)) {
            URI uri = Application.class.getClassLoader().getResource("Gene_Go.txt").toURI();
            Files.readAllLines(Paths.get(uri)).stream().map(s -> s.split("\\s")[1])
                    .forEach(parent -> processId(parent, writer));
        } catch (Exception x) {
            x.printStackTrace();
        }
        System.out.println("time:" + (System.currentTimeMillis() - time));
    }

    private static void processId(String parent, BufferedWriter writer) {
        try {
            if (count.incrementAndGet() > 100) {
                System.exit(0);
            }
            String url = String.format("http://www.ebi.ac.uk/QuickGO/GTerm?id=%s&format=mini", parent);
            Document document = Jsoup.connect(url).userAgent(USER_AGENT).get();
            document.select("table a[href]").stream().filter(e -> e.attr("href").startsWith("GTerm"))
                    .map(e -> e.attr("href").substring(9))
                    .forEach(child -> {
                        try {
                            writer.write(parent + ";" + child);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    });
            writer.flush();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}
