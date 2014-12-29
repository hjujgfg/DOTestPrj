package SScript;

import java.io.*;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Егор on 29.12.2014.
 */
public class DOTest {
    private static File packFile;
    private static final String configLocation = "config.conf";
    public static void main(String[] args) {
        //one argument - main pack file
        if (args.length == 1) {
            packFile = new File(args[0]);
            System.out.println("Initiating shuffling command");
            if (packFile.exists() && packFile.canRead() && packFile.canWrite()) {
                shuffleLines(packFile);
            } else {
                System.out.println("Error: Specified file can not be accessed");
                return;
            }
        } else if (args.length == 2) {
            boolean paramsAreInt = false;
            int players = -1;
            int cardsPerPlayer = -1;
            restoreFromConfig();
            try {
                players = Integer.parseInt(args[0]);
                cardsPerPlayer = Integer.parseInt(args[1]);
                System.out.println("Initiating serving command");
                serve(players, cardsPerPlayer);
                return;
            } catch (NumberFormatException e) {
                //Hypothetically it is not serving command and we have two players to collect from
            }
            File [] playersFiles = new File[args.length];
            for (int i = 0; i < args.length; i ++) {
                playersFiles[i] = new File(args[i]);
            }
            System.out.println("Initiating collecting command for 2 players");
            collectCards(playersFiles);
        } else if (args.length > 2) {
            restoreFromConfig();
            File [] playersFiles = new File[args.length];
            for (int i = 0; i < args.length; i ++) {
                playersFiles[i] = new File(args[i]);
            }
            System.out.println("Initiating collecting command for " + args.length + " players");
            collectCards(playersFiles);
        } else {
            System.out.println("Specify parameters");
        }
    }
    public static final int n = 52;

    /**
     * First method to be called.
     * Saves a file to work with and shuffles it.
     * Creates a configuration file with a path to main pack file
     * @param f - initial pack file
     */
    private static void shuffleLines(File f) {
        try {

            String[] pack = loadFile(f);
            if (pack == null) return;

            String str;
            Random random = new Random();
            int k;
            for (int i = 0; i < n; i ++) {
                k = random.nextInt(52);
                str = pack[i];
                pack[i] = pack[k];
                pack[k] = str;
            }

            BufferedWriter bw = new BufferedWriter(new FileWriter(f));
            for (int i = 0; i < 52; i ++ ) {
                bw.write(pack[i]+"\n");
            }
            bw.flush();
            bw.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println("Error: file not found");
            return;
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error: Writing error");
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(configLocation)));
            bw.write(packFile.getPath());
            bw.flush();
            bw.close();
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error: Config can not be saved");
            return;
        }
        System.out.println("Shuffling complete!");
    }

    /**
     * Method to recover pack from initial file
     */
    private static void restoreFromConfig() {
        try {
            BufferedReader br = new BufferedReader( new FileReader(configLocation));
            packFile = new File(br.readLine());
            br.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println("Error: Config file not found. Maybe you should specify main file first.");
            return;
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error: Config file corrupt. Start from the first step.");
            return;
        }
    }

    /**
     * Serves cards
     * @param playerCount
     * @param cardsPerPlayer
     */
    private static void serve(int playerCount, int cardsPerPlayer) {
        if (playerCount*cardsPerPlayer > n) {
            System.out.println("ERROR: Too many players or cards per player");
            return;
        }
        if (packFile == null || !packFile.exists()) {
            System.out.println("ERROR: Initial pack-file not set, use '-shuffle' first");
            return;
        }
        String [] file = loadFile(packFile);
        //if we receive null just stop
        if (file == null) {
            return;
        }
        try {
            FileWriter returnUnusedLines = new FileWriter(packFile);
            for (int i = playerCount*cardsPerPlayer; i < n; i ++) {
                returnUnusedLines.write(file[i]+"\n");
            }
            returnUnusedLines.flush();
            returnUnusedLines.close();
        } catch (IOException e) {
            System.out.println("Error: Initial file was changed, result may be corrupt");
            //e.printStackTrace();
        }
        int cardIndex = 0;
        FileWriter playerWriter = null;
        for (int i = 0; i < playerCount; i ++) {
            try {
                File player = new File("Player"+(i+1)+".txt");
                player.createNewFile();
                player.setReadable(true, false);
                player.setWritable(true, false);
                playerWriter = new FileWriter(player);
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Error: Unable to create a file for player " + (i + 1));
            }
            for (int j = 0; j < cardsPerPlayer; j ++) {
                try {
                    playerWriter.write(file[cardIndex] + "\n");
                    cardIndex ++;
                } catch (IOException e) {
                    //e.printStackTrace();
                    System.out.println("Error: Writing error");
                }
            }
            try {
                playerWriter.flush();
                playerWriter.close();
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
        System.out.println("Serving complete!");
    }

    /**
     * Loads lines from initial file. Clears the file.
     * @param file - file to work with
     * @return array of strings
     */
    private static String[] loadFile(File file) {
        String[] res = new String[n];
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            for (int i = 0; i < n; i++) {
                String tmp = br.readLine();
                if (tmp == null) {
                    System.out.println("Error: End of file reached, number of lines is less than " + n);
                    return null;
                }
                res [i] = tmp;

            }
            br.close();
        } catch (IOException e) {
            //e.printStackTrace();
            System.out.println("Error: Reading error");
            return null;
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
            pw.print("");
            pw.close();
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.out.println("Error: " + file.getPath() + " was externally modified");
        }
        return res;
    }

    /**
     * Appends lines to file
     * @param strings - array of strings to be appended
     * @return true if work is done, false - otherwise
     */
    private static boolean saveToMainFile(String [] strings) {
        try {
            FileWriter fw = new FileWriter(packFile, true);
            for (int i = 0; i < strings.length; i ++) {
                fw.write(strings[i] + "\n");
            }
            fw.flush();
            fw.close();
            return true;
        } catch (IOException e) {
            //e.printStackTrace();
            return false;
        }
    }

    /**
     * Collects cards from defined player files, removes player files
     * @param files
     */
    private static void collectCards(File[] files) {
        String [] cards;
        ArrayList<String> tempStrings = new ArrayList<String>();
        int cardsIndex = 0;
        for (int i = 0; i < files.length; i ++) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(files[i]));
                String readString;
                while ((readString = br.readLine()) != null) {
                    tempStrings.add(readString);
                    cardsIndex ++;
                }
                br.close();
                files[i].delete();

            } catch (FileNotFoundException e) {
                //e.printStackTrace();
                System.out.println("Error: File for player " + (i + 1) + " not found, skipping");
            } catch (FileSystemException e) {
                System.out.println("Error: File for player " + (i + 1) + " can not be accessed");
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Error: Writing error with player " + (i + 1));
            }
        }
        cards = tempStrings.toArray(new String[tempStrings.size()]);
        boolean bb = saveToMainFile(cards);
        if (bb) {
            System.out.println("Cards collected!");
        } else {
            System.out.println("Error: Players cards were not collected");
        }
    }
}
