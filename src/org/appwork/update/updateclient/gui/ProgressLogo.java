package org.appwork.update.updateclient.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.appwork.utils.swing.EDTRunner;

public class ProgressLogo extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    private final ImageIcon   icon;

    private float             progress         = 0.0f;

    private final Dimension   prefsize;

    private final ImageIcon   iconBorder;

    public ProgressLogo(final ImageIcon iconEnd, final ImageIcon iconStart) {
        this.icon = iconEnd;
        this.iconBorder = iconStart;

        this.prefsize = new Dimension(this.icon.getIconWidth(), this.icon.getIconHeight());
    }

    @Override
    public Dimension getPreferredSize() {
        return this.prefsize;
    }

    public float getProgress() {

        return this.progress;
    }

    @Override
    public void paint(final Graphics g) {
        g.setColor(this.getBackground());
        g.fillRect(0, 0, this.getWidth(), this.getHeight());
        final Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setClip(new Rectangle(this.prefsize.width, this.prefsize.height));

        this.iconBorder.paintIcon(this, g2, 0, 0);

        final int height = (int) (this.prefsize.height * this.progress);
        g2.setClip(0, this.prefsize.height - height, this.prefsize.height, height);

        this.icon.paintIcon(this, g2, 0, 0);

    }

    public void setProgress(final float prg) {

        this.progress = prg;

        new EDTRunner() {

            @Override
            protected void runInEDT() {
                ProgressLogo.this.setToolTipText("Update Installation Progress: " + (int) (ProgressLogo.this.progress * 100) + "%");

                ProgressLogo.this.repaint();

            }
        };

    }
}
