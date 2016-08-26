package Fabhaz;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.concurrent.TimeUnit;

import org.powerbot.script.PaintListener;

import org.powerbot.script.*;
import org.powerbot.script.rt4.ClientContext;
import org.powerbot.script.rt4.Component;
import org.powerbot.script.rt4.GameObject;
import org.powerbot.script.rt4.Item;

import java.util.concurrent.Callable;


@Script.Manifest(
        name = "Fab's OS Jewelry Crafter",
        description = "Crafts all Jewelry at Edgeville or Al Kharid.",
        properties = "author=Fabhaz;topic=1319500;client=4;"
)

public class JewelryCrafter extends PollingScript<ClientContext> implements PaintListener {

    private Tile bank_tile;
    private Tile furnace_tile;
    private int furnaceID;
    private static GUI form;
    private static Resources rsc;
    private static final Font ARIAL = new Font("Arial", Font.PLAIN, 12);
    private int startLevel = ctx.skills.level(12);
    private int startExp = ctx.skills.experience(12);
    private long startTime = System.currentTimeMillis();

    private int mouldID;
    private String mould;
    private String gem;
    private String location;
    private int component;
    private int gemID;
    private int lastXP;

    @Override
    public void start() {

        form = new GUI(this);
        rsc = new Resources();
        form.setLocationRelativeTo(null);
        form.setVisible(true);
        //wait for start to be pressed
        while(!form.valid) {
            Condition.sleep(500);
        }

        mould = form.mould;
        gem = form.gem;
        location = form.furnace;
        gemID = rsc.gemMap.get(gem);

        log.info("Crafting " + gem + " " + mould + "s" + " at " + location);
        getComponent();
        getLocation();
    }

    @Override
    public void poll() {
        final State state = getState();
        if (state == null) {
            return;
        }

        switch(state) {
            case CRAFT:

                //log.info("Crafting");

                if(ctx.players.local().animation() == -1){

                    ctx.objects.select(15).id(furnaceID).select(new Filter<GameObject>() {
                        @Override
                        public boolean accept(GameObject gameObject) {
                            return gameObject.inViewport();
                        }
                    });
                    
                    if (ctx.objects.isEmpty()) {
                        ctx.movement.step(furnace_tile);
                        ctx.camera.turnTo(furnace_tile);
                        Condition.wait(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                return furnace_tile.distanceTo(ctx.players.local()) < 3;
                        }
                        }, 250, 10);
                        break;
                    }

                    final Item gold = ctx.inventory.select().id(rsc.GOLD).poll();
                    final GameObject furnace = ctx.objects.nearest().poll();
                    final Component craft = ctx.widgets.component(rsc.WIDGET, component);

                    if (!craft.visible() && gold.interact("Use", gold.name()) && furnace.interact("Use", furnace.name())) {
                        Condition.wait(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                return craft.visible();
                            }
                        }, 250, 10);

                    } else if (craft.visible()) {
                        lastXP = ctx.skills.experience(12);

                        if(ctx.inventory.select().id(rsc.GOLD).count() >= 10) {

                            if (craft.interact("Make-X")) {
                                Condition.wait(new Callable<Boolean>() {
                                    @Override
                                    public Boolean call() throws Exception {
                                        return ctx.chat.pendingInput();
                                    }
                                }, 250, 10);
                            }

                            Condition.sleep(Random.nextInt(200, 500));

                            if (gemID != 0) {
                                if (Random.nextDouble() > 0.4) {
                                    ctx.input.sendln("13");
                                } else ctx.input.sendln(Integer.toString(Random.nextInt(13, 99)));
                            } else if (Random.nextDouble() > 0.4) {
                                ctx.input.sendln("27");
                            } else ctx.input.sendln(Integer.toString(Random.nextInt(27, 99)));

                        } else craft.interact("Make-10");

                        if (Random.nextDouble() > 0.85) {
                            if (Random.nextDouble() > 0.3) {
                                checkSkill(13);
                            } else checkSkill(Random.nextInt(1, 22));
                        }

                        Condition.wait(new Callable<Boolean>() {
                            @Override
                            public Boolean call() throws Exception {
                                if(ctx.inventory.select().id(rsc.GOLD).count() != 0 && (ctx.inventory.select().id(gemID).count() != 0 || gemID == 0)
                                        && ctx.skills.experience(12) > lastXP) {
                                    lastXP = ctx.skills.experience(12);
                                    return false;
                                } else return true;
                            }
                        }, 3400, 10);
                    }
                }

                break;

            case BANKING:

                //log.info("Banking");

                if (!ctx.bank.inViewport()) {
                    ctx.movement.step(bank_tile);
                    ctx.camera.turnTo(bank_tile);
                    Condition.wait(new Callable<Boolean>() {
                        @Override
                        public Boolean call() throws Exception {
                            return bank_tile.distanceTo(ctx.players.local()) < 5;
                        }
                    }, 250, 10);
                } else if (ctx.bank.open()) {

                    if (ctx.bank.select().id(rsc.GOLD).count(true) < 3 || (ctx.bank.select().id(gemID).count(true) < 3 && gemID != 0)) {
                        ctx.bank.depositAllExcept(mouldID);
                        log.info("Out of required items");
                        ctx.controller.stop();
                    } else{
                        ctx.bank.depositAllExcept(mouldID);

                        if(ctx.inventory.select().id(mouldID).count() == 0){
                            ctx.bank.withdraw(mouldID, 1);
                        }

                        if(gemID != 0) {
                            if (Random.nextDouble() > 0.7) {
                                ctx.bank.withdraw(rsc.GOLD, 13);
                                ctx.bank.withdraw(gemID, 13);
                            } else {
                                ctx.bank.withdraw(gemID, 13);
                                ctx.bank.withdraw(rsc.GOLD, 13);
                            }
                        } else ctx.bank.withdraw(rsc.GOLD, 27);
                    }
                }

                break;
        }
    }

    private JewelryCrafter.State getState() {
        if (ctx.inventory.select().id(rsc.GOLD).count() == 0 || (ctx.inventory.select().id(gemID).count() == 0 && gemID != 0) || ctx.inventory.select().id(mouldID).count() == 0) {
            return State.BANKING;
        }
        return State.CRAFT;
    }

    public void stop(){
        ctx.controller.stop();
    }

    private enum State {
        CRAFT,BANKING
    }

    private void checkSkill(int skillNumber) {
        //open skill tab and hover over the given skill
        Condition.sleep(Random.nextInt(300, 1000));
        ctx.widgets.component(548, 53).click();
        ctx.widgets.component(320, skillNumber).hover();

        Condition.wait(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                if(ctx.inventory.select().id(rsc.GOLD).count() != 0 && (ctx.inventory.select().id(gemID).count() != 0 || gemID == 0)
                        && ctx.skills.experience(12) > lastXP) {
                    lastXP = ctx.skills.experience(12);
                    return false;
                } else return true;
            }
        }, Random.nextInt(1000, 1300), 10);

        ctx.widgets.component(548, 48).click();

    }

    public void getComponent(){
        // cant do switch statement with strings in Java SE 6
        // input the gemID into the component map to find the correct component
        // the map used depends on the mould
        if(mould.equals("Ring")){
            mouldID = 1592;
            component = rsc.ringComponentMap.get(gemID);
        } else if(mould.equals("Necklace")){
            mouldID = 1597;
            component = rsc.necklaceComponentMap.get(gemID);
        } else if(mould.equals("Amulet")) {
            mouldID = 1595;
            component = rsc.amuletComponentMap.get(gemID);
        } else if(mould.equals("Bracelet")) {
            mouldID = 11065;
            component = rsc.braceletComponentMap.get(gemID);
        }
    }

    public void getLocation(){
        if(location.equals("Al Kharid")){
            bank_tile = rsc.AL_KHARID_BANK_TILE;
            furnace_tile = rsc.AL_KHARID_FURNACE_TILE;
            furnaceID = rsc.AL_KHARID_FURNACE;
        } else {
            bank_tile = rsc.EDGEVILLE_BANK_TILE;
            furnace_tile = rsc.EDGEVILLE_FURNACE_TILE;
            furnaceID = rsc.EDGEVILLE_FURNACE;
        }
    }

    @Override
    public void repaint(Graphics graphics) {
        final Graphics2D g = (Graphics2D) graphics;

        Point mouse = ctx.input.getLocation();

        g.setColor(Color.GREEN);
        g.drawLine((int) mouse.getX() - 10, (int) mouse.getY(), (int) mouse.getX() + 10, (int) mouse.getY());
        g.drawLine((int) mouse.getX(), (int) mouse.getY() - 10, (int) mouse.getX(), (int) mouse.getY() + 10);

        g.setFont(ARIAL);
        Color color = new Color(0, 0, 0, 200);
        g.setColor(color);
        g.fillRect(5, 5, 160, 85);
        g.setColor(Color.WHITE);

        long runtime = System.currentTimeMillis() - startTime;
        java.lang.String hms = java.lang.String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(runtime),
                TimeUnit.MILLISECONDS.toMinutes(runtime) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(runtime) % TimeUnit.MINUTES.toSeconds(1));

        g.drawString("Jewellery Crafter - Fabhaz", 10, 20);
        g.drawString("Runtime: " + hms, 10, 40);
        g.drawString("Level: " + startLevel + " + " + "(" + (ctx.skills.level(12) - startLevel) + ")", 10, 60);
        int expGained = ctx.skills.experience(12) - startExp;
        g.drawString("XP(XP/h): " + expGained + "(" + Math.round(((float) expGained / runtime) * 3600000) + ")", 10, 80);
    }


}