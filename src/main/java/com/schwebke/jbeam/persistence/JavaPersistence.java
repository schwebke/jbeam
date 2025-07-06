package com.schwebke.jbeam.persistence;

import com.schwebke.jbeam.model.SelectableModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * Java serialization persistence implementation.
 * Maintains compatibility with existing .jbm files.
 */
public class JavaPersistence implements ModelPersistence {
    
    @Override
    public void save(SelectableModel model, OutputStream outputStream) throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream)) {
            objectOutputStream.writeObject(model);
        }
    }
    
    @Override
    public SelectableModel load(InputStream inputStream) throws IOException, ClassNotFoundException {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)) {
            return (SelectableModel) objectInputStream.readObject();
        }
    }
    
    @Override
    public String getFileExtension() {
        return "jbm";
    }
    
    @Override
    public String getFormatDescription() {
        return "JBeam Data Files";
    }
}