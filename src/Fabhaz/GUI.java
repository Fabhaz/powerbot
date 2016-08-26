package Fabhaz;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

 class GUI extends JFrame{
    private JPanel p = new JPanel();
    private JButton startButton = new JButton("Start");
    private JButton cancelButton = new JButton("Cancel");
    private Resources rsc;
     boolean valid = false;
    String gem;
    String mould;
    String furnace;



    GUI(final JewelryCrafter instance){

        setSize(550, 90);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Jewellery Crafter");
        rsc = new Resources();

        final JComboBox mouldComboBox = new JComboBox(rsc.moulds);
        final JComboBox gemComboBox = new JComboBox(rsc.gems);
        final JComboBox furnaceComboBox = new JComboBox(rsc.furnaces);


        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                mould = rsc.moulds[mouldComboBox.getSelectedIndex()];
                gem = rsc.gems[gemComboBox.getSelectedIndex()];
                furnace = rsc.furnaces[furnaceComboBox.getSelectedIndex()];
                valid = true;
                if(gem.equals("Enchanted") && !mould.equals("Ring")){
                    JOptionPane.showMessageDialog(GUI.this, "Enchanted gems can only be used to make slayer rings.");
                    instance.stop();
                    dispose();
                }
                dispose();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                valid = true;
                instance.stop();
                dispose();
            }
        });

        setLayout(new BorderLayout());
        p.add(new JLabel("Mould:"));
        p.add(mouldComboBox);
        p.add(new JLabel("Gem:"));
        p.add(gemComboBox);
        p.add(new JLabel("Location:"));
        p.add(furnaceComboBox);
        p.add(startButton);
        p.add(cancelButton);
        add(p);

    }


}