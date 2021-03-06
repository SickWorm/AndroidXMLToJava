package <!PACKAGE_NAME>;

import <!PACKAGE_NAME>.values.colors;
import <!PACKAGE_NAME>.values.strings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Get the resources with int id.
 * This file is used in output directory to manager the resources. No meaning to the AXML transform project.
 * @author sickworm
 *
 */
public class Resources {
    private Context context = null;
    
    public Resources(Context context) {
        this.context = context;
    }
    
    public String getString(int id) {
        return strings.map.get(id);
    }
    
    public Drawable getDrawable(int id) {
        return (Drawable) drawables.get(context, id);
    }
    
    public ColorStateList getColorStateList(int id) {
        return (ColorStateList) drawables.get(context, id);
    }
    
    public int getColor(int id) {
        return colors.map.get(id);
    }
    
    public View getLayout(int id) {
        return layouts.get(context, id);
    }
}