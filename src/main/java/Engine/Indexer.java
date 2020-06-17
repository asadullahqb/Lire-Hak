package Engine;

import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.*;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

public class Indexer {
    public static void main(String[] args) {
        // TODO: Probably start the GUI here.
    }

    public void ParallelIndexing(String[] args) {
        int threads = 6;
        boolean passed = false;

        if (args.length > 0) {
            File f =  new File(args[0]);
            // Output file path
            System.out.println("Indexing images in " + args[0]);
            if (f.exists() && f.isDirectory())
                passed = true;
        }
        if (!passed) {
            System.out.println("No directory is found or given as first argument");
            System.out.println("Run \"ParallelIndexing <directory>\" to index files of a directory.");
            System.exit(1);
        }

        // Check threads
        if (args.length > 1) {
            if (args[1].matches("\\d+")) {
                threads = Math.max(Integer.parseInt(args[1]), 64);
                System.out.println("Threads in use " + threads);
            }
        }

        // use ParallelIndexer to index all photos.
        ParallelIndexer indexer = new ParallelIndexer(threads, "index", args[0]);

        // Global feature builders
        indexer.addExtractor(CEDD.class);
        indexer.addExtractor(FCTH.class);
        indexer.addExtractor(AutoColorCorrelogram.class);
        indexer.addExtractor(SimpleColorHistogram.class);
        indexer.addExtractor(Tamura.class);
        indexer.addExtractor(EdgeHistogram.class);

        indexer.run();
        System.out.println("Finished indexing.");
    }

    public void Indexing(String[] args) throws IOException {
        boolean passed = false;
        if (args.length > 0) {
            File f =  new File(args[0]);
            // Output file path
            System.out.println("Indexing images in " + args[0]);
            if (f.exists() && f.isDirectory())
                passed = true;
        }
        if (!passed) {
            System.out.println("No directory is found or given as first argument");
            System.out.println("Run \"ParallelIndexing <directory>\" to index files of a directory.");
            System.exit(1);
        }

        // TODO: Check this out
        // gdb.extractGlobalFeature()

        // Get all images from directory
        ArrayList<String> images = FileUtils.getAllImages(new File(args[0]), true);

        GlobalDocumentBuilder gdb = new GlobalDocumentBuilder(false, false);

        // Global feature builders
        gdb.addExtractor(CEDD.class);
        gdb.addExtractor(FCTH.class);
        gdb.addExtractor(AutoColorCorrelogram.class);
        gdb.addExtractor(SimpleColorHistogram.class);
        gdb.addExtractor(Tamura.class);
        gdb.addExtractor(EdgeHistogram.class);

        // Creating Lucene IndexWriter
        IndexWriter iw = LuceneUtils.createIndexWriter("index", true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);

        // Iterating through images building the low level features
        for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
            String imageFilePath = it.next();
            System.out.println("Indexing " + imageFilePath);
            try {
                BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                Document document = gdb.createDocument(img, imageFilePath);
                iw.addDocument(document);
            } catch (Exception e) {
                System.err.println("Error reading image or indexing it.");
                e.printStackTrace();
            }
        }

        // Closing the IndexWriter
        iw.close();
        System.out.println("Finished indexing.");
    }
}
