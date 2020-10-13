package kz.shinov;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CardReader {
    static final int[] X = {142, 214, 285, 357, 428};//координаты карт
    static final String[] r_template = {"2.png","3.png","4.png","5.png","6.png","7.png","8.png","9.png", "10.png","J.png","Q.png","K.png","A.png"};
    static final String[] s_template = {"c.png","d.png","h.png","s.png"};
    static Map<String,String> ranks = new HashMap<String,String>();//номиналы
    static Map<String,String> suits = new HashMap<String,String>();//масти
    //Возвращает бинарную строку - ч/б массив изображения
    static String getBinary(BufferedImage symbol){
        int r, g, b;
        double[][] yM = new double[symbol.getWidth()][symbol.getHeight()];
        double y = 0,avgY = 0;
        for(int i=0; i<symbol.getWidth(); i++) {
            for(int j=0; j<symbol.getHeight(); j++) {
                Color c = new Color(symbol.getRGB(i, j));
                r = c.getRed();
                g = c.getGreen();
                b = c.getBlue();
                //Считаем яркость каждого пикселя по формуле википедии Y'=0.2126R+0.7152G+0.0722B
                y = 0.2126 * r + 0.7152 * g + 0.0722 * b;
                yM[i][j] = y;
                avgY = avgY + y;
            }
        }
        //Конверт в Ч/Б и сразу в строку
        StringBuilder binaryString = new StringBuilder();
        avgY = avgY/(symbol.getWidth() * symbol.getHeight());
        for(int i=0; i<symbol.getWidth(); i++) {
            for(int j=0; j<symbol.getHeight(); j++) {
                if( yM[i][j] > avgY){
                    binaryString.append(0);//symbol.setRGB(i, j, Color.WHITE.getRGB());
                } else {
                    binaryString.append(1);//symbol.setRGB(i, j, Color.BLACK.getRGB());
                }
            }
        }
        return binaryString.toString();
    }

    static void loadSamples(String[] templates, Map<String, String> map) throws IOException {
        ClassLoader classLoader = CardReader.class.getClassLoader();
        for(String file : templates){
            BufferedImage sample = ImageIO.read(classLoader.getResourceAsStream("resource/"+file));
            map.put(file.replace(".png",""), getBinary(sample));
        }
    }
    //Расстояние Хэмминга https://ru.wikipedia.org/wiki/%D0%A0%D0%B0%D1%81%D1%81%D1%82%D0%BE%D1%8F%D0%BD%D0%B8%D0%B5_%D0%A5%D1%8D%D0%BC%D0%BC%D0%B8%D0%BD%D0%B3%D0%B0
    static int hammingDistance(String str1, String str2){
       int counter = 0;
       for (int i = 0; i < str1.length(); i++)
        {
            if (str1.charAt(i) != str2.charAt(i)) counter++;
        }
        return counter;
    }

    static String getValue(String binaryString, Map<String, String> map){
        int min = 150;//значение подобрано, результат сверялся с функцией левинштейна
        String findSymbol = "";
        for (Map.Entry<String, String> entry : map.entrySet()) {
            int hammingDistance = hammingDistance(binaryString, entry.getValue());
            if (hammingDistance < min) {
                min = hammingDistance;
                findSymbol = entry.getKey();
            }
        }
        return findSymbol;
    }
    public static void main(String[] args) throws IOException {
        if(args.length == 0){
            System.out.println("Путь к файлам не задан!");
            return;
        }
        String dir =args[0];
        loadSamples(r_template,ranks);
        loadSamples(s_template,suits);
        File[] files = new File(dir).listFiles((d, n) -> n.endsWith(".png"));
        for(File file : files){
            StringBuilder str = new StringBuilder();
            BufferedImage image = ImageIO.read(file);
            for (int i : X) {
                BufferedImage symbol = image.getSubimage(i + 4, 588, 35, 32);//цифры
                str.append(getValue(getBinary(symbol), ranks));
                symbol = image.getSubimage(i + 26, 634, 32, 32);//масти
                str.append(getValue(getBinary(symbol), suits));
            }
            System.out.println(file.getName()+" - "+str.toString());
        }
    }
}
