/*
 * Copyright (C) 2020 Malte Schulze.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library;  If not, see 
 * <https://www.gnu.org/licenses/>.
 */
package ssms.qol;

import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.campaign.TextPanelAPI;
import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import org.apache.log4j.Level;
import ssms.qol.ui.LogHandler;

/**
 *
 * @author Malte Schulze
 */
public class InteractionDialog_TextPanelLogHandler implements LogHandler {
    protected WeakReference<InteractionDialogAPI> dialog;
    protected boolean includeStacktrace;
    
    public InteractionDialog_TextPanelLogHandler(InteractionDialogAPI dialog) {
        this(dialog, false);
    }
    
    public InteractionDialog_TextPanelLogHandler(InteractionDialogAPI dialog, boolean includeStacktrace) {
        this.dialog = new WeakReference<>(dialog);
        this.includeStacktrace = includeStacktrace;
    }
    
    @Override
    public void log(Level level, String msg, Throwable t) {
        if ( level == Level.OFF || dialog == null ) return;
        InteractionDialogAPI d = dialog.get();
        if ( d == null ) return;
        TextPanelAPI tp = d.getTextPanel();
        if ( tp == null ) return;
        Color c;
        switch ( level.toInt() ) {
            case Level.DEBUG_INT: c = Color.green; break;
            case Level.ALL_INT: 
            case Level.INFO_INT: c = Color.white; break;
            case Level.WARN_INT: c = Color.orange; break;
            case Level.ERROR_INT: c = Color.red; break;
            case Level.FATAL_INT: c = Color.pink; break;
            default: c = Color.gray; break;
        }
        //starsector "banned" PrintWriter and PrintStream
        /*try (StringWriter writer = new StringWriter(); PrintWriter pwriter = new PrintWriter(writer)) {
            pwriter.append(msg);
            if ( includeStacktrace && t != null ) {
                pwriter.println();
                t.printStackTrace(pwriter);
            }
            pwriter.flush();
            tp.addParagraph(writer.toString(), c);
        } catch (IOException ex) {}*/
        StringBuilder sb = new StringBuilder(msg);
        if ( includeStacktrace && t != null ) {
            sb.append("\n");
            sb.append(t).append("\n");
            StackTraceElement[] trace = t.getStackTrace();
            for (StackTraceElement traceElement : trace)
                sb.append("at ").append(traceElement).append("\n");
        }
        tp.addParagraph(sb.toString(), c);
    }
    
}
