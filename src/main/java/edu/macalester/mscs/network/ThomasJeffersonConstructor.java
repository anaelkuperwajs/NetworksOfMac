package edu.macalester.mscs.network;

public class ThomasJeffersonConstructor extends MatrixConstructor{

    public static final String CHARACTER_FILE_NAME = "src/main/resources/data/characters/tjWordListFinal.csv";
    public static final String TEXT_FILE_NAME = "src/main/resources/text/tjCritique.txt";
    public static final String BOOK_PREFIX = "TJ";

    /**
     * Main method for generating the matrix, edge list and log files for Thomas Jefferson excerpts.
     * @param args
     */
    public static void main(String[] args) {
        int fileNum = 1;
        String fileDesc = "thomasJeff";

        ThomasJeffersonConstructor tjConstructor = new ThomasJeffersonConstructor(fileNum,15, 1);

        tjConstructor.constructMatrix(fileDesc, DEFAULT_LOG_FOLDER);
        tjConstructor.writeFiles(fileDesc, DEFAULT_LOG_FOLDER, false);
    }

    public ThomasJeffersonConstructor(int file_num, int radius, int noise) {
        super(BOOK_PREFIX + file_num, TEXT_FILE_NAME, CHARACTER_FILE_NAME, radius, noise);
    }
}
