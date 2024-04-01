package at.aau.serg.dktserver.communication.utilities;

import at.aau.serg.dktserver.communication.ActionJsonObject;
import at.aau.serg.dktserver.communication.ConnectJsonObject;
import at.aau.serg.dktserver.communication.Wrapper;
import at.aau.serg.dktserver.communication.enums.Request;
import com.google.gson.Gson;

public class WrapperHelper {
    private static Gson gson = new Gson();

    public static Object getInstanceFromWrapper(Wrapper wrapper){
        if (wrapper.getClassname() == null || wrapper.getClassname().isEmpty())
            return null;

        String object = gson.toJson(wrapper.getObject());

        switch (wrapper.getClassname()){
            case "ConnectJsonObject" -> {
                return gson.fromJson(object, ConnectJsonObject.class);
            }
            case "ActionJsonObject" -> {
                return gson.fromJson(object, ActionJsonObject.class);
            }

        }
        return null;
    }

    public static Object getInstanceFromJson(String json){
        Wrapper wrapper;
        try {
            wrapper = gson.fromJson(json, Wrapper.class);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
        return getInstanceFromWrapper(wrapper);
    }

    public static String toJsonFromObject(int gameId, Request request, Object object){
        Wrapper wrapper = new Wrapper(object.getClass().getSimpleName(), gameId, request, object);
        return gson.toJson(wrapper);
    }
}
