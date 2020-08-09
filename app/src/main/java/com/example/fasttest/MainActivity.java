package com.example.fasttest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.JsonReader;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.fasttest.entities.User;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Context context = this;
    ListView lw;
    String filter = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lw = findViewById(R.id.a_main_list_users);
        new TaskGetUsersList().execute(filter);

        final EditText search = findViewById(R.id.a_main_fie_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filter = search.getText().toString();
                new TaskGetUsersList().execute(filter);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        Button button = findViewById(R.id.a_main_button_add);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.userlayoutfields);
                Button button1 = dialog.findViewById(R.id.userlayout_button);
                final TextView login = dialog.findViewById(R.id.userlayout_login);
                final TextView pass = dialog.findViewById(R.id.userlayout_pass);
                final TextView name = dialog.findViewById(R.id.userlayout_name);
                final TextView date = dialog.findViewById(R.id.editTextDate);
                date.setClickable(true);
                date.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                               date.setText(datePicker.getYear() + "-" + datePicker.getMonth() + 1 + "-" + datePicker.getDayOfMonth());
                            }
                        }, 2020, 11 ,1);
                        datePickerDialog.show();
                    }
                });
                button1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new TaskUserAdd().execute(new User(0, login.getText().toString(), pass.getText().toString(), name.getText().toString()));
                       // new SimpleDialog("Дата рождения: " + datePicker.toString());
                    }
                });
                dialog.show();
            }
        });

        registerForContextMenu(lw);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.usercontextmenu, menu);
    }


    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        final User user = (User) info.targetView.getTag();
        switch (item.getItemId()) {
            case R.id.usercontext_dell:
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setMessage("Вы действительно хотите удалить этого (" + user.ID + ") пользователя");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new TaskUserDelete().execute(user);
                    }
                });
                alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });
                alertDialog.create().show();
                return true;
            case R.id.usercontext_edit:
                Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.userlayoutfields);
                Button button = dialog.findViewById(R.id.userlayout_button);
                button.setText("Изменить");
                final TextView login = dialog.findViewById(R.id.userlayout_login);
                final TextView pass = dialog.findViewById(R.id.userlayout_pass);
                final TextView name = dialog.findViewById(R.id.userlayout_name);
                login.setText(user.login);
                pass.setText(user.pass);
                name.setText(user.name);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        user.login = login.getText().toString();
                        user.pass = pass.getText().toString();
                        user.name = name.getText().toString();
                        new TaskUserEdit().execute(user);
                    }
                });
                dialog.show();
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    //ПРОСТОЙ ДИАЛОГ С 1 КНОПКОЙ "ОК"
    public class SimpleDialog {
        public SimpleDialog(String s) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
            alertDialog.setMessage(s);
            alertDialog.setNegativeButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            alertDialog.create().show();
        }
    }


    ////////////////////////////////////////////////////////////////////////////////
    //АДАПТЕР ДЛЯ ПОЛЬЗОВАТЕЛЯ
    public class UserAdapter extends ArrayAdapter<User> {

        public UserAdapter(@NonNull Context context, int resource, @NonNull List<User> objects) {
            super(context, resource, objects);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            User user = getItem(position);
            if(convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.foradapteruser, null);
            convertView.setTag(user);
            TextView ID = convertView.findViewById(R.id.useradapt_id);
            TextView Login = convertView.findViewById(R.id.useradapt_login);
            TextView Pass = convertView.findViewById(R.id.useradapt_pass);
            TextView Name = convertView.findViewById(R.id.useradapt_name);
            ID.setText("Пользователь №" + user.ID);
            Login.setText("Login: " + user.login);
            Pass.setText("Pass: " + user.pass);
            Name.setText("Name: " + user.name);
            return convertView;
        }
    }
    ///////////////////////////////////////////////////////////////////////////////////////

    //////////////////////////////////////////////////////
    // КЛАССЫ ASYNC ЗАПРОСОВ

    //USERLIST
    public class TaskGetUsersList extends AsyncTask<String, Integer, ArrayList<User>> {

        @Override
        protected ArrayList<User> doInBackground(String... filter) {
            try {
                String fil = filter.length == 0 ? "" : filter[0];
                HttpURLConnection conn = User.getConnection("http://188.120.248.48:20080/product/read2.php?filter="+fil, "GET");
                JsonReader reader = new JsonReader(new InputStreamReader(conn.getInputStream()));
                ArrayList<User> users = new ArrayList<User>();
                reader.beginObject();
                reader.nextName();
                reader.beginArray();
                while(reader.hasNext()) {
                    users.add(User.parseUserFromJSON(reader));
                }
                reader.endArray();
                return users;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ArrayList<User>();
        }

        @Override
        protected void onPostExecute(ArrayList<User> users) {
            super.onPostExecute(users);
            lw.setAdapter(new UserAdapter(context, R.layout.foradapteruser, users));
        }
    }
    //Удаление пользователя
    public class TaskUserDelete extends AsyncTask<User, Integer, String> {
        @Override
        protected String doInBackground(User... users) {
            try {
                return users[0].delete();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new SimpleDialog(s);
            new TaskGetUsersList().execute(filter);
        }
    }
    //Изменение пользователя
    public class TaskUserEdit extends AsyncTask<User, Integer, String> {
        @Override
        protected String doInBackground(User... users) {
            try {
                return users[0].edit();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new SimpleDialog(s);
            new TaskGetUsersList().execute(filter);
        }
    }
    //Добавление пользователя
    public class TaskUserAdd extends AsyncTask<User, Integer, String> {
        @Override
        protected String doInBackground(User... users) {
            try {
                return users[0].add();
            } catch (IOException e) {
                e.printStackTrace();
                return e.getMessage();
            } catch (JSONException e) {
                e.printStackTrace();
                return e.getMessage();
            }
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new SimpleDialog(s);
            new TaskGetUsersList().execute(filter);
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////
}