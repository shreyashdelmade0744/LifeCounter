package com.marceljurtz.lifecounter.Game;

import android.content.SharedPreferences;

import com.marceljurtz.lifecounter.Helper.ClickType;
import com.marceljurtz.lifecounter.Helper.Color;
import com.marceljurtz.lifecounter.Helper.Game;
import com.marceljurtz.lifecounter.Helper.MagicColor;
import com.marceljurtz.lifecounter.Helper.Operator;
import com.marceljurtz.lifecounter.Helper.Player;
import com.marceljurtz.lifecounter.Helper.PlayerID;
import com.marceljurtz.lifecounter.Helper.PreferenceManager;

public class GamePresenter implements IGamePresenter {

    private SharedPreferences preferences;
    private Game game;
    private IGameView view;

    private boolean settingsVisible;
    private boolean poisonVisible;
    private boolean powerSaveEnabled;

    private Player player1;
    private Player player2;
    private Player player3;
    private Player player4;

    private final int SCREEN_SMALL = 1;
    private final int SCREEN_NORMAL = 2;
    private final int SCREEN_LARGE = 3;
    private final int SCREEN_XLARGE = 4;

    private boolean hideOtherControlsWhenSettingsDisplayed = false;

    public GamePresenter(IGameView view, SharedPreferences preferences) {
        this.preferences = preferences;
        this.view = view;

        int screenLayout = view.GetScreenSize();
        // Configuration.SCREENLAYOUT_SIZE_LARGE;   --> 3
        // Configuration.SCREENLAYOUT_SIZE_NORMAL;  --> 2
        // Configuration.SCREENLAYOUT_SIZE_SMALL;   --> 1
        // Configuration.SCREENLAYOUT_SIZE_XLARGE;  --> 4

        if(screenLayout != SCREEN_XLARGE && view.GetPlayerAmount() == 4) hideOtherControlsWhenSettingsDisplayed = true;

        player1 = new Player(PlayerID.ONE); // SCHEISSE
        player2 = new Player(PlayerID.TWO);

        if(view.GetPlayerAmount() == 4) {
            player3 = new Player(PlayerID.THREE);
            player4 = new Player(PlayerID.FOUR);
            game = new Game(preferences, new Player[]{player1, player2, player3, player4});
        } else {
            game = new Game(preferences, new Player[]{player1, player2});
        }


        // Initiate default colors
        view.InitColorButton(MagicColor.BLACK, PreferenceManager.getDefaultBlack(preferences));
        view.InitColorButton(MagicColor.BLUE, PreferenceManager.getDefaultBlue(preferences));
        view.InitColorButton(MagicColor.GREEN, PreferenceManager.getDefaultGreen(preferences));
        view.InitColorButton(MagicColor.RED, PreferenceManager.getDefaultRed(preferences));
        view.InitColorButton(MagicColor.WHITE, PreferenceManager.getDefaultWhite(preferences));

        // Settings, Energy-Saving & Poison
        view.DisableSettingsControls(hideOtherControlsWhenSettingsDisplayed, false);
        view.SettingsButtonDisable();
        settingsVisible = false;

        view.PoisonButtonDisable();
        view.DisablePoisonControls(hideOtherControlsWhenSettingsDisplayed);
        poisonVisible = false;
    }

    @Override
    public void OnCreate() {

    }

    @Override
    public void OnPause() {
        game.SaveGameState(preferences);
    }

    @Override
    public void OnResume() {

        if(view.GetPlayerAmount() == 4) {
            game.LoadGameState(preferences, 4);
            view.SetLayoutColor(PlayerID.ONE, game.GetPlayers()[0].GetColorOrDefault().GetIntValue());
            view.SetLayoutColor(PlayerID.TWO, game.GetPlayers()[1].GetColorOrDefault().GetIntValue());
            view.SetLayoutColor(PlayerID.THREE, game.GetPlayers()[2].GetColorOrDefault().GetIntValue());
            view.SetLayoutColor(PlayerID.FOUR, game.GetPlayers()[3].GetColorOrDefault().GetIntValue());
        } else {
            game.LoadGameState(preferences, 2);
            view.SetLayoutColor(PlayerID.ONE, game.GetPlayers()[0].GetColorOrDefault().GetIntValue());
            view.SetLayoutColor(PlayerID.TWO, game.GetPlayers()[1].GetColorOrDefault().GetIntValue());
        }

        for(Player player : game.GetPlayers()) {
            view.SetLifepoints(player.GetPlayerID(), player.GetLifePoints() + "");
            view.SetPoisonpoints(player.GetPlayerID(), player.GetPoisonPoints() + "");
        }

        settingsVisible = false;
        view.DisableSettingsControls(hideOtherControlsWhenSettingsDisplayed, poisonVisible);
        view.SettingsButtonDisable();

        poisonVisible = false;
        view.DisablePoisonControls(hideOtherControlsWhenSettingsDisplayed);
        view.PoisonButtonDisable();

        powerSaveEnabled = false;

        view.InitColorButton(MagicColor.BLACK, PreferenceManager.getCustomColor(preferences, MagicColor.BLACK));
        view.InitColorButton(MagicColor.BLUE, PreferenceManager.getCustomColor(preferences, MagicColor.BLUE));
        view.InitColorButton(MagicColor.GREEN, PreferenceManager.getCustomColor(preferences, MagicColor.GREEN));
        view.InitColorButton(MagicColor.RED, PreferenceManager.getCustomColor(preferences, MagicColor.RED));
        view.InitColorButton(MagicColor.WHITE, PreferenceManager.getCustomColor(preferences, MagicColor.WHITE));

        if(PreferenceManager.getScreenTimeoutDisabled(preferences)) {
            view.DisableScreenTimeout();
        } else {
            view.EnableScreenTimeout();
        }

        view.HideNavigationDrawer();
    }

    @Override
    public void OnDestroy() {

    }

    /*
    public void setSettingsVisible() {
        settingsVisible = !settingsVisible;
        if(settingsVisible) {
            view.EnableSettingsControls();
        } else {
            view.DisableSettingsControls();
        }
    }

    public boolean getSettingsVisible() {
        return settingsVisible;
    }
*/
    /*public boolean getPoisonVisible() {
        return poisonVisible;
    }*/

    public void togglePowerSavingMode() {
        powerSaveEnabled = !powerSaveEnabled;
        if(powerSaveEnabled) {
            view.EnableEnergySaving(PreferenceManager.powerSave, PreferenceManager.powerSaveTextcolor);
        } else {
            view.DisableEnergySaving(PreferenceManager.getDefaultBlack(preferences), PreferenceManager.regularTextcolor);
        }
        view.SetDrawerTextPowerSaving(!powerSaveEnabled);
    }

    /*

    public boolean getPowerSaveEnabled() {
        return powerSaveEnabled;
    }

    //endregion
*/
    @Override
    public void OnLifeUpdate(PlayerID playerID, ClickType clickType, Operator operator) {
        game.updateLifepoints(playerID, clickType, operator);
        int points = game.getPlayerLifepoints(playerID);

        String pointsStr = String.format("%02d",points);
        view.SetLifepoints(playerID, pointsStr);
    }

    @Override
    public void OnPoisonUpdate(PlayerID playerID, ClickType clickType, Operator operator) {
        game.updatePoisonpoints(playerID, clickType, operator);
        int points = game.getPlayerPoisonpoints(playerID);

        String pointsStr = String.format("%02d",points);
        view.SetPoisonpoints(playerID, pointsStr);
    }



    @Override
    public void OnColorButtonClick(PlayerID playerID, MagicColor color, ClickType clickType) {
        if(clickType.equals(ClickType.SHORT)) {

            // Disable PowerSaveMode if enabled
            if(powerSaveEnabled) togglePowerSavingMode();

            Color newColor;

            switch (color) {
                case BLUE:
                    newColor = new Color(MagicColor.BLUE, PreferenceManager.getCustomColor(preferences, MagicColor.BLUE));
                    break;
                case GREEN:
                    newColor = new Color(MagicColor.GREEN, PreferenceManager.getCustomColor(preferences, MagicColor.GREEN));
                    break;
                case RED:
                    newColor = new Color(MagicColor.RED, PreferenceManager.getCustomColor(preferences, MagicColor.RED));
                    break;
                case WHITE:
                    newColor = new Color(MagicColor.WHITE, PreferenceManager.getCustomColor(preferences, MagicColor.WHITE));
                    break;
                default:
                    newColor = new Color(MagicColor.BLACK, PreferenceManager.getCustomColor(preferences, MagicColor.BLACK));
                    break;
            }

            switch(playerID) {
                case ONE:
                    player1.SetColor(newColor);
                    game.GetPlayers()[0].SetColor(newColor);
                    break;
                case TWO:
                    player2.SetColor(newColor);
                    game.GetPlayers()[1].SetColor(newColor);
                    break;
                case THREE:
                    player3.SetColor(newColor);
                    game.GetPlayers()[2].SetColor(newColor);
                    break;
                case FOUR:
                    player4.SetColor(newColor);
                    game.GetPlayers()[3].SetColor(newColor);
                    break;
                default:
            }

            view.SetLayoutColor(playerID, newColor.GetIntValue());
        } else if(clickType.equals(ClickType.LONG) && color.equals(MagicColor.BLACK)) {
            togglePowerSavingMode();
        }
    }

    @Override
    public void OnPoisonButtonClick() {
        // Activation only possible on small screens when settings are hidden
        if(!hideOtherControlsWhenSettingsDisplayed || !settingsVisible || view.GetPlayerAmount() == 2) {
            poisonVisible = !poisonVisible;
            if(poisonVisible) {
                view.EnablePoisonControls(hideOtherControlsWhenSettingsDisplayed);
                view.PoisonButtonEnable();
            } else {
                view.DisablePoisonControls(hideOtherControlsWhenSettingsDisplayed);
                view.PoisonButtonDisable();
            }
        }
    }

    @Override
    public void OnSettingsButtonClick(ClickType clickType) {
        if(clickType.equals(ClickType.LONG)) {
            view.LoadSettingsActivity();
        } else {
            settingsVisible = !settingsVisible;
            if(settingsVisible) {
                view.EnableSettingsControls(hideOtherControlsWhenSettingsDisplayed, poisonVisible && hideOtherControlsWhenSettingsDisplayed);
                view.SettingsButtonEnable();
            } else {
                view.DisableSettingsControls(hideOtherControlsWhenSettingsDisplayed, poisonVisible && hideOtherControlsWhenSettingsDisplayed);
                view.SettingsButtonDisable();
            }
        }
    }

    @Override
    public void OnResetButtonClick() {
        player1.ResetPoints(preferences);
        player2.ResetPoints(preferences);

        settingsVisible = false;
        poisonVisible = false;
        view.DisablePoisonControls(hideOtherControlsWhenSettingsDisplayed);
        view.SettingsButtonDisable();
        view.DisableSettingsControls(hideOtherControlsWhenSettingsDisplayed, poisonVisible);
        view.PoisonButtonDisable();

        view.SetLifepoints(PlayerID.ONE, String.format("%02d",player1.GetLifePoints()));
        view.SetLifepoints(PlayerID.TWO, String.format("%02d",player2.GetLifePoints()));

        view.SetPoisonpoints(PlayerID.ONE, String.format("%02d",player1.GetPoisonPoints()));
        view.SetPoisonpoints(PlayerID.TWO, String.format("%02d",player2.GetPoisonPoints()));

        if(view.GetPlayerAmount() == 4) {
            player3.ResetPoints(preferences);
            player4.ResetPoints(preferences);

            view.SetLifepoints(PlayerID.THREE, String.format("%02d",player3.GetLifePoints()));
            view.SetLifepoints(PlayerID.FOUR, String.format("%02d",player4.GetLifePoints()));

            view.SetPoisonpoints(PlayerID.THREE, String.format("%02d",player3.GetPoisonPoints()));
            view.SetPoisonpoints(PlayerID.FOUR, String.format("%02d",player4.GetPoisonPoints()));
        }
    }

    //region NavDrawer Handling
    @Override
    public void OnMenuEntryTogglePlayerTap() {
        if(view.GetPlayerAmount() == 4) {
            // Load 2 Player View
            PreferenceManager.saveDefaultPlayerAmount(preferences, 2);
        } else if(view.GetPlayerAmount() == 2) {
            // Load 4 Player View
            PreferenceManager.saveDefaultPlayerAmount(preferences, 4);
        }
        view.HideNavigationDrawer();
        view.RestartActivity();
    }

    @Override
    public void OnMenuEntryDicingTap() {
        view.LoadDicingActivity();
    }

    @Override
    public void OnMenuEntryEnergySaveTap() {
        togglePowerSavingMode();
    }

    @Override
    public void OnMenuEntrySettingsTap() {
        OnSettingsButtonClick(ClickType.LONG);
    }

    @Override
    public void OnMenuEntryAboutTap() {
        view.LoadAboutActivity();
    }

    @Override
    public void OnMenuEntryCounterManagerTap() {
        view.LoadCounterManagerActivity();
    }
    //endregion
}
