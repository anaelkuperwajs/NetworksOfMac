package edu.macalester.mscs.network;

public class ObamaConstructor extends MatrixConstructor{

    public static final String CHARACTER_FILE_NAME = "src/main/resources/data/characters/ObamaCritiqueWordList.csv";
    public static final String TEXT_FILE_NAME = "src/main/resources/text/ObamaCritique.txt";
    public static final String BOOK_PREFIX = "Obama";

    /**
     * Main method for generating the matrix, edge list and log files for Thomas Jefferson excerpts.
     * @param args
     */
    public static void main(String[] args) {
        int fileNum = 1;
        String fileDesc = "Obama";

        ObamaConstructor obamaConstructor = new ObamaConstructor(fileNum,15, 1);

        obamaConstructor.constructMatrix(fileDesc, DEFAULT_LOG_FOLDER);
        obamaConstructor.writeFiles(fileDesc, DEFAULT_LOG_FOLDER, false);
    }

    public ObamaConstructor(int file_num, int radius, int noise) {
        super(BOOK_PREFIX + file_num, TEXT_FILE_NAME, CHARACTER_FILE_NAME, radius, noise);
    }
}
