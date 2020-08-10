package com.example.fasttest.entities;

import android.os.AsyncTask;
import android.util.JsonReader;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class User {
    @NonNull
    @Override
    public String toString() {
        return login;
    }

    public int ID;
    public String login, pass, name;
    public User() { }
    public User(int ID, String login, String pass, String name) {
        this.ID = ID;
        this.login = login;
        this.pass = pass;
        this.name = name;
    }

    public static HttpURLConnection getConnection(String url, String method) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod(method);
        return conn;
    }

    public static User parseUserFromJSON(JsonReader jsonReader) throws IOException {
        User u = new User();
        jsonReader.beginObject();
        while(jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                case "ID":
                    u.ID = jsonReader.nextInt();
                    break;
                case "login":
                    u.login = jsonReader.nextString();
                    break;
                case "pass":
                    u.pass = jsonReader.nextString();
                    break;
                case "name":
                    u.name = jsonReader.nextString();
                    break;
                default:
                    jsonReader.skipValue();
            }
        }
        jsonReader.endObject();
        return u;
    }

    public String getReqMessage(HttpURLConnection conn) throws IOException, JSONException {
        InputStream stream;
        if(conn.getResponseCode() >= 200 && conn.getResponseCode() < 300)
            stream = conn.getInputStream();
        else
            stream = conn.getErrorStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        StringBuilder total = new StringBuilder();
        for(String line; (line = r.readLine()) != null;) {
            total.append(line).append("\n");
        }
        JSONObject obj = new JSONObject(total.toString());
        String out = obj.getString("message");
        return out;
    }

    public String add() throws IOException, JSONException {
        HttpURLConnection conn = getConnection("http://188.120.248.48:20080/product/create.php", "POST");
        JSONObject obj = new JSONObject();
        obj.put("login", this.login);
        obj.put("pass", this.pass);
        obj.put("name", this.name);
        conn.getOutputStream().write(obj.toString().getBytes());
        conn.getOutputStream().flush();
        return getReqMessage(conn);
    }

    public String edit() throws IOException, JSONException {
        HttpURLConnection conn = getConnection("http://188.120.248.48:20080/product/update.php","PUT");
        conn.getOutputStream().write(("ID="+this.ID+"&login="+this.login+"&pass="+this.pass+"&name="+this.name).getBytes());
        conn.getOutputStream().flush();
        return getReqMessage(conn);
    }

    public String delete() throws IOException, JSONException {
        HttpURLConnection conn = getConnection("http://188.120.248.48:20080/product/delete.php","DELETE");
        conn.getOutputStream().write(("ID="+this.ID).getBytes());
        conn.getOutputStream().flush();
        return getReqMessage(conn);
    }


}
