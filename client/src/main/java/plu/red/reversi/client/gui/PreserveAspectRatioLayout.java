package plu.red.reversi.client.gui;

import java.awt.*;

/**
 * A simple, custom layout manager, that preserves the preferred aspect ratio of the
 * child component, centering the component within the container.  It is only
 * intended for use with a single component within the parent.
 */
public class PreserveAspectRatioLayout implements LayoutManager {

    public void addLayoutComponent(String name, Component comp) {
        // Do nothing..  No need to store component.
    }

    public void removeLayoutComponent(Component comp) {
        // Nothing needed here.
    }

    public Dimension preferredLayoutSize(Container parent) {
        Dimension d = new Dimension(0,0);
        Insets insets = parent.getInsets();
        int nComps = parent.getComponentCount();
        if(nComps > 0) {

            // We only use the first one.  Ignore others.
            Component c = parent.getComponent(0);

            d.width = c.getPreferredSize().width + insets.left + insets.right;
            d.height = c.getPreferredSize().height + insets.top + insets.bottom;
        }

        return d;
    }

    public Dimension minimumLayoutSize(Container parent) {
        Dimension d = new Dimension(0,0);
        Insets insets = parent.getInsets();
        int nComps = parent.getComponentCount();
        if(nComps > 0) {

            // We only use the first one.  Ignore others.
            Component c = parent.getComponent(0);

            d.width = c.getMinimumSize().width + insets.left + insets.right;
            d.height = c.getMinimumSize().height + insets.top + insets.bottom;
        }

        return d;
    }

    public void layoutContainer(Container parent) {
        Dimension d = new Dimension(0,0);
        Insets insets = parent.getInsets();

        int width = parent.getWidth() - (insets.left + insets.right);
        int height = parent.getHeight() - (insets.top + insets.bottom);

        double arParent = (double) width / height;

        int nComps = parent.getComponentCount();
        if(nComps > 0) {

            // We only use the first one.  Ignore others.
            Component c = parent.getComponent(0);

            Dimension pref = c.getPreferredSize();
            double arComp = (double)pref.width / pref.height;

            if( arComp > arParent ) {
                int w = width;
                int h = Math.round((float)(w / arComp));
                int extraSpace = height - h;
                c.setBounds(insets.left, extraSpace / 2 + insets.top, w, h);
            } else {
                int h = height;
                int w = Math.round( (float)(arComp * h) );
                int extraSpace = width - w;
                c.setBounds(extraSpace / 2 + insets.left, insets.top, w, h);
            }
        }
    }
}
