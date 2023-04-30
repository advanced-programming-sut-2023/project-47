package Model;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;

public class Map {
    private String name;
    private Square[][] squares;
    private int width;
    private int length;

    public Map(String name, int width, int length) {
        this.name = name;
        this.width = width;
        this.length = length;
        this.squares = new Square[width][length];
        //create squares
    }

    public void setSquares(Square[][] squares) {
        this.squares = squares;
    }

    public Square[][] getSquares() {
        return squares;
    }

    public Square getSquareFromMap(int x, int y) {
        return squares[x][y];
    }

    public int getWidth() {
        return width;
    }

    public int getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public static void saveMap(Map map, String fileName) {
        // filename only without address only name
        // will save it in resources/Map/<fileName>
        try {
            String fileAddress = "src/main/resources/Map/" + fileName + ".json";
            FileWriter file = new FileWriter(fileAddress);
            Gson gson = new Gson();
            gson.toJson(map, file);
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadMap(String fileName) {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map>(){}.getType();
            String fileAddress = "src/main/resources/Map/" + fileName + ".json";
            File f = new File(fileAddress);
            if(f.exists() && !f.isDirectory())
                DataBase.setSelectedMap(gson.fromJson(new FileReader(fileAddress), type));
            else
                DataBase.setSelectedMap(null);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
