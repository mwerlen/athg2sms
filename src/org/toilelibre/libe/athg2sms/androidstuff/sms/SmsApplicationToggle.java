package org.toilelibre.libe.athg2sms.androidstuff.sms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import org.toilelibre.libe.athg2sms.actions.Actions;
import org.toilelibre.libe.athg2sms.androidstuff.api.storage.SharedPreferencesHolder;

import java.lang.reflect.InvocationTargetException;

import static org.toilelibre.libe.athg2sms.androidstuff.api.storage.PreferencesBinding.BINDING_GLOBAL_NAME;

public class SmsApplicationToggle {
    private static String       ACTION_CHANGE_DEFAULT = "android.provider.Telephony.ACTION_CHANGE_DEFAULT";
    private static final String EXTRA_PACKAGE_NAME    = "package";
    public static final int REQUEST_CODE = 42;

    public void toggleDefault(Activity activity) {
        SharedPreferencesHolder<SharedPreferences> preferences =
                new SharedPreferencesHolder<SharedPreferences>(activity.getSharedPreferences (BINDING_GLOBAL_NAME, 0));

        if (android.os.Build.VERSION.SDK_INT >= 19) {
            final String myPackageName = activity.getPackageName ();
            if (!myPackageName.equals(this.getDefaultSmsPackage (activity))) {
                new Actions().saveDefaultSmsApp(preferences, this.getDefaultSmsPackage (activity));
                final Intent intentSetDefault = new Intent (SmsApplicationToggle.ACTION_CHANGE_DEFAULT);
                intentSetDefault.putExtra (SmsApplicationToggle.EXTRA_PACKAGE_NAME, myPackageName);
                activity.startActivityForResult (intentSetDefault, REQUEST_CODE);
            } else {
                final String packageName = new Actions().getDefaultSmsApp(preferences);
                final Intent intentSetDefault = new Intent (SmsApplicationToggle.ACTION_CHANGE_DEFAULT);
                intentSetDefault.putExtra (SmsApplicationToggle.EXTRA_PACKAGE_NAME, packageName);
                activity.startActivityForResult (intentSetDefault, REQUEST_CODE);

            }
        }

    }

    public String getDefaultSmsPackage (final Context context) {
        String result = "";
        try {
            final Class<?> c = Class.forName ("android.provider.Telephony$Sms");
            result = (String) c.getMethod ("getDefaultSmsPackage", Context.class).invoke (null, context);
        } catch (final ClassNotFoundException e) {
        } catch (final SecurityException e) {
        } catch (final NoSuchMethodException e) {
        } catch (final IllegalArgumentException e) {
        } catch (final IllegalAccessException e) {
        } catch (final InvocationTargetException e) {
        }
        return result;
    }

}
