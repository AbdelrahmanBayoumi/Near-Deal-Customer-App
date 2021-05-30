package com.mohamed_amgd.ayzeh.repo;

import android.content.Context;
import android.util.Patterns;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Pattern;

import id.zelory.compressor.Compressor;

public class Util {
    public static final String DATE_FORMAT = "dd/MM/yyyy";
    private static Util mInstance;

    private Util() {
    }

    public static Util getInstance() {
        if (mInstance == null) {
            mInstance = new Util();
        }
        return mInstance;
    }

    public boolean isEmailValid(String email) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    public boolean isUsernameValid(String username) {
        Pattern pattern = Pattern.compile("^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$");
        return pattern.matcher(username).matches();
    }

    public boolean isPasswordValid(String password) {
        Pattern pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
        return pattern.matcher(password).matches();
    }

    public boolean isConfirmPasswordValid(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    public boolean isBirthdateValid(String birthdateInput) {
        Pattern pattern = Pattern.compile("^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]))\\1|(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2])\\2))(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$|^(?:29(\\/|-|\\.)0?2\\3(?:(?:(?:1[6-9]|[2-9]\\d)?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))$|^(?:0?[1-9]|1\\d|2[0-8])(\\/|-|\\.)(?:(?:0?[1-9])|(?:1[0-2]))\\4(?:(?:1[6-9]|[2-9]\\d)?\\d{2})$");
        if(pattern.matcher(birthdateInput).matches()){
            SimpleDateFormat sdf = new SimpleDateFormat(Util.DATE_FORMAT, Locale.US);
            Date birthdate = null;
            try {
                birthdate = sdf.parse(birthdateInput);
            } catch (ParseException e) {
                return false;
            }
            Date currentDate = Calendar.getInstance().getTime();
            long diffInDays = (currentDate.getTime() - birthdate.getTime())/(1000L*60L*60L*24L*365L);
            return diffInDays >= 12;
        } else {
            return false;
        }
    }

    public File getCompressedImageFile(Context context, String imagePath){
        try {
            return new Compressor(context).compressToFile(new File(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new File(imagePath);
    }
}
