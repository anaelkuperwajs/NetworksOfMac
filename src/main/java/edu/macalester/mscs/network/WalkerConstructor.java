package edu.macalester.mscs.network;

public class WalkerConstructor extends  MatrixConstructor{

    public static final String CHARACTER_FILE_NAME = "src/main/resources/data/characters/WalkerWordList.csv";
    public static final String TEXT_FILE_NAME = "src/main/resources/text/WalkerArticle.txt";
    public static final String BOOK_PREFIX = "Walker";

    /**
     * Main method for generating the matrix, edge list and log files for Thomas Jefferson excerpts.
     * @param args
     */
    public static void main(String[] args) {
        int fileNum = 1;
        String fileDesc = "Walker";

        WalkerConstructor walkerConstructor = new WalkerConstructor(fileNum,15, 1);

        walkerConstructor.constructMatrix(fileDesc, DEFAULT_LOG_FOLDER);
        walkerConstructor.writeFiles(fileDesc, DEFAULT_LOG_FOLDER, false);
    }

    public WalkerConstructor(int file_num, int radius, int noise) {
        super(BOOK_PREFIX + file_num, TEXT_FILE_NAME, CHARACTER_FILE_NAME, radius, noise);
    }
}
