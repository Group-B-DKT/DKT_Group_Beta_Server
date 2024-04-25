package at.aau.serg.dktserver.model.io;

import at.aau.serg.dktserver.model.domain.Field;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;

public class CSVReader {
        public static ArrayList<Field> readFields() {
            ArrayList<Field> list = new ArrayList<>();
            String path = "fields.csv";
            String line = "";
            try (BufferedReader br = new BufferedReader(new FileReader(path))) {
                System.out.println(br.readLine());
                while ((line = br.readLine()) != null) {
                    String[] values = line.split(";");
                    list.add(new Field(Integer.parseInt(values[0]), values[1], Boolean.parseBoolean(values[2])));

                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
            return list;
        }

}

