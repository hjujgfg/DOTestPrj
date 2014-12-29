package SScript;

import java.io.*;
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

        if (args.length == 1) {
            packFile = new File(args[0]);
            if (packFile.exists() && packFile.canRead() && packFile.canWrite()) {
                shuffleLines(packFile);
            } else {
                System.out.print("Error: Specified file can not be accessed");
                return;
            }
        } else if (args[0].equalsIgnoreCase("-serve")) {
            restoreFromConfig();
            int players = -1;
            int numCards = -1;
            try {
                players = Integer.parseInt(args[1]);
            } catch (NumberFormatException ex) {
                System.out.print("Error: incorrect format (number of players)");
                return;
            }
            try {
                numCards = Integer.parseInt(args[2]);
            } catch (NumberFormatException ex) {
                System.out.print("Error: incorrect format (number of cards)");
                return;
            }

            serve(players, numCards);
        } else if (args[0].equalsIgnoreCase("-collect")) {
            restoreFromConfig();
            File [] players = new File[args.length - 1];
            for (int i = 1; i < args.length; i ++) {
                players[i - 1] = new File(args[i]);
            }
            collectCards(players);
        } else {
            System.out.print("Error: Unknown parameters");
        }
    }
    public static final int n = 52;
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
            e.printStackTrace();
            System.out.print("Error: file not found");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("Error: Writing error");
            return;
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(configLocation)));
            bw.write(packFile.getPath());
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("Error: Config can not be saved");
            return;
        }
    }

    private static void restoreFromConfig() {
        try {
            BufferedReader br = new BufferedReader( new FileReader(configLocation));
            packFile = new File(br.readLine());
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.print("Error: Config file not found. Maybe you should specify main file first.");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("Error: Config file corrupt. Start from the first step.");
            return;
        }
    }
    private static void serve(int playerCount, int cardsPerPlayer) {
        if (playerCount*cardsPerPlayer > n) {
            System.out.print("ERROR: Too many players or cards per player");
            return;
        }
        if (packFile == null || !packFile.exists()) {
            System.out.print("ERROR: Initial pack-file not set, use '-shuffle' first");
            return;
        }
        String [] file = loadFile(packFile);
        try {
            FileWriter returnUnusedLines = new FileWriter(packFile);
            for (int i = playerCount*cardsPerPlayer; i < n; i ++) {
                returnUnusedLines.write(file[i]+"\n");
            }
            returnUnusedLines.flush();
            returnUnusedLines.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        int cardIndex = 0;
        FileWriter playerWriter = null;
        for (int i = 0; i < playerCount; i ++) {
            try {
                File player = new File("Player"+(i+1)+".txt");
                player.createNewFile();
                playerWriter = new FileWriter(player);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.print("Error: Unable to create a file for player "+ (i+1));
            }
            for (int j = 0; j < cardsPerPlayer; j ++) {
                try {
                    playerWriter.write(file[cardIndex] + "\n");
                    cardIndex ++;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.print("Error: Writing error");
                }
            }
            try {
                playerWriter.flush();
                playerWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private static String[] loadFile(File file) {
        String[] res = new String[n];
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            for (int i = 0; i < n; i++) {
                String tmp = br.readLine();
                if (tmp == null) {
                    System.out.print("Error: End of file reached, number of lines is less than " + n);
                    return null;
                }
                res [i] = tmp;

            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.print("Error: Reading error");
            return null;
        }
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(file);
            pw.print("");
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return res;
    }
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
            e.printStackTrace();
            return false;
        }
    }
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
                files[i].delete();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                System.out.print("Error: File for player " + (i + 1) + " not found, skipping");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.print("Error: Writing error");
            }
        }
        cards = tempStrings.toArray(new String[tempStrings.size()]);
        boolean bb = saveToMainFile(cards);
        if (bb) {
            System.out.print("Players cards were collected successfully");
        } else {
            System.out.print("Error: Players cards were not collected");
        }
    }
}
