package pl.kasyno;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Random;

public class KasynoListener implements Listener {

    private final Random random = new Random();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Sprawdz czy to nasze GUI
        if (!title.equals(KasynoCommand.KASYNO_TITLE)) return;
        
        Inventory clickedInventory = event.getClickedInventory();
        int slot = event.getSlot();
        
        // Klikniecie w inventory gracza - pozwol
        if (clickedInventory == player.getInventory()) {
            return;
        }
        
        // Klikniecie w slot 13 (slot na przedmiot) - pozwol
        if (slot == 13) {
            return;
        }
        
        // Dla innych slotow - zablokuj
        event.setCancelled(true);
        
        // Obsluga przyciskow
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        // Przycisk GRAJ (slot 11)
        if (slot == 11 && clicked.getType() == Material.LIME_CONCRETE) {
            handlePlay(player, event.getInventory());
        }
        
        // Przycisk ZAMKNIJ (slot 15)
        if (slot == 15 && clicked.getType() == Material.RED_CONCRETE) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        String title = event.getView().getTitle();
        if (!title.equals(KasynoCommand.KASYNO_TITLE)) return;
        
        // Pozwol tylko na przeciaganie do slotu 13
        for (int slot : event.getRawSlots()) {
            if (slot != 13 && slot < 27) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        
        Player player = (Player) event.getPlayer();
        String title = event.getView().getTitle();
        
        if (!title.equals(KasynoCommand.KASYNO_TITLE)) return;
        
        // Oddaj przedmiot ze slotu 13 jesli jakis jest
        Inventory inventory = event.getInventory();
        ItemStack item = inventory.getItem(13);
        
        if (item != null && item.getType() != Material.AIR) {
            HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            
            // Jesli nie ma miejsca - wyrzuc na ziemie
            for (ItemStack left : leftover.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), left);
            }
            
            player.sendMessage("§6[Kasyno] §eZwrocono twoj przedmiot.");
        }
    }

    private void handlePlay(Player player, Inventory gui) {
        ItemStack betItem = gui.getItem(13);
        
        // Sprawdz czy jest przedmiot
        if (betItem == null || betItem.getType() == Material.AIR) {
            player.sendMessage("§c[Kasyno] §cWloz przedmiot w srodkowy slot!");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }
        
        // Sprawdz czy to nie jest element GUI
        if (isGuiItem(betItem)) {
            player.sendMessage("§c[Kasyno] §cTo nie jest prawidlowy przedmiot!");
            return;
        }
        
        // Animacja - dzwiek losowania
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        
        // LOSOWANIE 50/50
        boolean win = random.nextBoolean();
        
        // Opoznienie dla efektu dramatycznego
        Bukkit.getScheduler().runTaskLater(KasynoPlugin.getInstance(), () -> {
            
            if (win) {
                // WYGRANA - podwojenie przedmiotu
                handleWin(player, gui, betItem);
            } else {
                // PRZEGRANA - utrata przedmiotu
                handleLoss(player, gui, betItem);
            }
            
        }, 20L); // 1 sekunda opoznienia
        
        // Tymczasowa informacja
        player.sendMessage("§6[Kasyno] §eLosowanie...");
    }

    private void handleWin(Player player, Inventory gui, ItemStack item) {
        int originalAmount = item.getAmount();
        int maxStack = item.getMaxStackSize();
        int doubledAmount = originalAmount * 2;
        
        // Wyczyszczenie slotu
        gui.setItem(13, null);
        
        // Daj podwojony przedmiot
        if (doubledAmount <= maxStack) {
            ItemStack doubled = item.clone();
            doubled.setAmount(doubledAmount);
            giveItem(player, doubled);
        } else {
            int remaining = doubledAmount;
            while (remaining > 0) {
                ItemStack stack = item.clone();
                int toGive = Math.min(remaining, maxStack);
                stack.setAmount(toGive);
                giveItem(player, stack);
                remaining -= toGive;
            }
        }
        
        // Efekty wygranej
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 1.0f);
        
        player.sendMessage("");
        player.sendMessage("§a§l  ★ WYGRANA! ★");
        player.sendMessage("§7  Twoj przedmiot zostal §a§lPODWOJONY§7!");
        player.sendMessage("§7  Otrzymujesz: §e" + doubledAmount + "x §f" + formatItemName(item));
        player.sendMessage("");
        
        // Broadcast
        Bukkit.broadcastMessage("§6[Kasyno] §a" + player.getName() + 
            " §7wygral i podwoil §e" + originalAmount + "x " + formatItemName(item) + "§7!");
    }

    private void handleLoss(Player player, Inventory gui, ItemStack item) {
        int lostAmount = item.getAmount();
        String itemName = formatItemName(item);
        
        // Usuniecie przedmiotu (przegrana)
        gui.setItem(13, null);
        
        // Efekty przegranej
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.5f, 0.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_DESTROY, 1.0f, 1.0f);
        
        player.sendMessage("");
        player.sendMessage("§c§l  ✖ PRZEGRANA! ✖");
        player.sendMessage("§7  Twoj przedmiot zostal §c§lUTRACONY§7!");
        player.sendMessage("§7  Straciles: §c" + lostAmount + "x §f" + itemName);
        player.sendMessage("");
        
        // Broadcast
        Bukkit.broadcastMessage("§6[Kasyno] §c" + player.getName() + 
            " §7przegral i stracil §e" + lostAmount + "x " + itemName + "§7!");
    }

    private void giveItem(Player player, ItemStack item) {
        HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        
        for (ItemStack left : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), left);
            player.sendMessage("§e[Kasyno] §7Brak miejsca w ekwipunku - przedmiot wyrzucony na ziemie!");
        }
    }

    private boolean isGuiItem(ItemStack item) {
        Material type = item.getType();
        return type == Material.GRAY_STAINED_GLASS_PANE ||
               type == Material.YELLOW_STAINED_GLASS_PANE ||
               type == Material.LIME_CONCRETE ||
               type == Material.RED_CONCRETE ||
               type == Material.GOLD_INGOT;
    }

    private String formatItemName(ItemStack item) {
        if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        String name = item.getType().toString().toLowerCase().replace("_", " ");
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
