package org.toilelibre.libe.athg2sms.actions;

import org.toilelibre.libe.athg2sms.androidstuff.api.activities.ContextHolder;
import org.toilelibre.libe.athg2sms.androidstuff.api.activities.HandlerHolder;
import org.toilelibre.libe.athg2sms.androidstuff.api.storage.FileRetriever;
import org.toilelibre.libe.athg2sms.androidstuff.api.storage.SharedPreferencesHolder;
import org.toilelibre.libe.athg2sms.androidstuff.sms.SmsDeleter;
import org.toilelibre.libe.athg2sms.androidstuff.sms.SmsInserter;
import org.toilelibre.libe.athg2sms.business.convert.ConvertException;
import org.toilelibre.libe.athg2sms.business.convert.ConvertFormatGuesser;
import org.toilelibre.libe.athg2sms.business.convert.Converter;
import org.toilelibre.libe.athg2sms.business.export.Exporter;
import org.toilelibre.libe.athg2sms.business.pattern.Format;
import org.toilelibre.libe.athg2sms.business.pattern.FormatSettings;
import org.toilelibre.libe.athg2sms.business.preferences.AppPreferences;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;

public class Actions {

    public void saveDefaultSmsApp (SharedPreferencesHolder<?> preferencesHolder, String smsPackage) {
        new AppPreferences(preferencesHolder).saveDefaultSmsApp (smsPackage);
    }

    public String getDefaultSmsApp(SharedPreferencesHolder<?> preferences) {
        return new AppPreferences(preferences).getDefaultSmsApp ();
    }

    public String guessNow(String content) throws FileNotFoundException {
        return new ConvertFormatGuesser().guessNow(content);
    }

    public String[] getAllFormats() {
        return FormatSettings.getInstance().getFormats().keySet().toArray(new String [FormatSettings.getInstance().getFormats().size ()]);
    }

    public void runConversionNow(ContextHolder<?> contextHolder, Runnable done, ErrorHandler errorHandler, String filename, String pattern) {
        final Converter thisConverter = new Converter();
        ProcessRealTimeFeedback convertListener = ProcessRealTimeFeedback.getInstance();
        final String content = this.getContentFromFileName (contextHolder, filename);
        if (content == null) {
            return;
        }
        boolean atLeastOneConverted;
        try {
            atLeastOneConverted = thisConverter.convertNow(FormatSettings.getInstance().getFormats().get(
                    pattern), content,
                    convertListener, new HandlerHolder<>(convertListener.getHandler()),
                    contextHolder, new SmsInserter(), new SmsDeleter());
        } catch (ConvertException ce) {
            errorHandler.run(ce);
            return;
        } finally {
            ProcessRealTimeFeedback.unbind();
        }
        if (!atLeastOneConverted)
            errorHandler.run(new ParseException("No SMS Imported !\nThe selected format does not match the input", 0));
        else
            done.run();
    }


    private String getContentFromFileName (ContextHolder<?> context, String filename) {
        try {
            return FileRetriever.getFile (context, filename);
        } catch (FileNotFoundException e) {
            ProcessRealTimeFeedback.unbind();
            return null;
        }
    }

    public void saveAskedPermissions(SharedPreferencesHolder<?> preferences, String[] permissions) {
        new AppPreferences(preferences).saveAskedPermissions (permissions);
    }

    public boolean askedPermissionsContainsAll(SharedPreferencesHolder<?> preferences, String[] permissions) {
        return new AppPreferences(preferences).getAskedPermissions ().containsAll (Arrays.asList (permissions));
    }

    public void exportNow(Object context, File tempFile, Runnable afterExport, String pattern) {

        final ProcessRealTimeFeedback convertListener = ProcessRealTimeFeedback.getInstance();
        final String result = new Exporter().export(new ContextHolder<>(context),
                new HandlerHolder<>(convertListener.getHandler()), pattern, convertListener);

        try {
            FileWriter fw = new FileWriter(tempFile);
            fw.write(result);
            fw.close();

            afterExport.run();
        } catch (IOException e) {
            throw new ConvertException("Exporting the sms did not work because of a problem with the storage", e);
        }
    }

    public void saveFormats(SharedPreferencesHolder<?> preferences) {
        new AppPreferences(preferences).saveFormats(FormatSettings.getInstance().getFormats());
    }

    public void removeFormat(String pattern) {
        FormatSettings.getInstance().getFormats().remove(pattern);
    }

    public void addOrChangeFormat(String patternName, String pattern, String exportFormat, String inbox, String sent) {
        Format format = new Format(patternName, pattern, exportFormat.isEmpty() ? pattern : exportFormat, inbox, sent);
        FormatSettings.getInstance().addOrChangeFormats(format);
    }

    public Format.FormatRegexRepresentation getFormatRegex(String pattern) {
        return FormatSettings.getInstance().getFormats().get(pattern).getRegex();
    }
}
