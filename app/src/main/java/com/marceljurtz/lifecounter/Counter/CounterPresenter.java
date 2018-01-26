package com.marceljurtz.lifecounter.Counter;

import android.content.SharedPreferences;
import android.widget.LinearLayout;

import com.marceljurtz.lifecounter.Helper.Color;
import com.marceljurtz.lifecounter.Helper.Counter;
import com.marceljurtz.lifecounter.Helper.MagicColor;
import com.marceljurtz.lifecounter.Helper.Player;
import com.marceljurtz.lifecounter.Helper.PlayerID;
import com.marceljurtz.lifecounter.Helper.PreferenceManager;

import java.util.ArrayList;
import java.util.Random;

public class CounterPresenter implements ICounterPresenter {

    private ArrayList<Player> players;
    private ICounterView view;
    private SharedPreferences preferences;
    private Random random;

    public CounterPresenter(ICounterView view, SharedPreferences preferences, ArrayList<Player> players) {
        this.players = players;
        this.preferences = preferences;
        this.view = view;
        random = new Random();
    }

    @Override
    public void OnCreate() {
        int num = random.nextInt(4);
        Color color = new Color(MagicColor.WHITE);
        switch(num) {
            case 0:
                color = new Color(MagicColor.BLUE);
                break;
            case 1:
                color = new Color(MagicColor.GREEN);
                break;
            case 2:
                color = new Color(MagicColor.RED);
                break;
            case 3:
                break;
        }
        view.SetBackgroundColor(color);
    }

    @Override
    public void OnPause() {
        PreferenceManager.SavePlayerCounterData(preferences, players);
    }

    @Override
    public void OnResume() {
        // Load items from preferences
        players = PreferenceManager.LoadPlayerCounterData(preferences);

        // Reload View
        view.DeleteAllCounters();

        for (Player player : players) {

            view.SetPlayerIdentificationText(player.GetPlayerID(), player.GetPlayerIdentification());

            for (Counter counter : player.GetAllCounters()) {
                view.AddCounter(player.GetPlayerID(), counter);
            }
        }
    }

    @Override
    public void OnDestroy() {

    }

    @Override
    public void OnFloatingActionButtonTap() {
        view.LoadCounterAddDialog(players);
    }

    @Override
    public void AddCounter(PlayerID playerId, Counter counter) {
        for (Player player : players) {
            if (player.GetPlayerID().equals(playerId)) {
                counter.SetIdentifier(player.GetPlayerID().toString() + "-" + player.GetAllCounters().size());
                player.AddCounter(counter);
                break;
            }
        }

        view.AddCounter(playerId, counter);
    }

    @Override
    public void OnPlayerIdentificationChangeConfirmed(PlayerID playerId, String newIdentification) {

        for (Player player : players) {
            if (player.GetPlayerID() == playerId) {
                player.SetPlayerIdentification(newIdentification);
                break;
            }
        }

        view.SetPlayerIdentificationText(playerId, newIdentification);
    }

    @Override
    public void OnMenuEntryTwoPlayerClick() {
        PreferenceManager.saveDefaultPlayerAmount(preferences, 2);
        view.LoadGameActivity();
    }

    @Override
    public void OnMenuEntryFourPlayerClick() {
        PreferenceManager.saveDefaultPlayerAmount(preferences, 4);
        view.LoadGameActivity();
    }

    @Override
    public void OnMenuEntryDicingClick() {
        view.LoadDicingActivity();
    }

    @Override
    public void OnMenuEntrySettingsClick() {
        view.LoadSettingsActivity();
    }

    @Override
    public void OnMenuEntryAboutClick() {
        view.LoadAboutActivity();
    }

    @Override
    public String GetPlayerIdentification(PlayerID playerID) {
        for (Player player : players) {
            if (player.GetPlayerID() == playerID) {
                return player.GetPlayerIdentification();
            }
        }
        return "";
    }

    @Override
    public void OnPlayerIdentificationTap(PlayerID playerID) {
        view.LoadPlayerIdentificationDialog(playerID, GetPlayerIdentification(playerID));
    }

    @Override
    public void OnPlayerIdentificationLongTap(PlayerID playerID) {
        view.LoadPlayerDeletionDialog(playerID);
    }

    @Override
    public void OnPlayerDeletionConfirmed(PlayerID playerID) {
        for (Player player : players) {
            if (player.GetPlayerID() == playerID) {
                player.ClearCounters();
                player.SetPlayerIdentification("");
                break;
            }
        }
        view.DeleteAllCountersForPlayer(playerID);
    }

    @Override
    public void OnCounterDeletionConfirmed(LinearLayout counterLayout) {
        boolean deleteParent = false;

        Counter currentCounter = GetCounterByIdentifier(counterLayout.getTag().toString());
        Player currentPlayer = GetPlayerByCounterIdentifier(counterLayout.getTag().toString());

        if (currentPlayer != null && currentCounter != null) {
            currentPlayer.RemoveCounter(currentCounter);
            if (currentPlayer.GetAllCounters().size() <= 0) {
                deleteParent = true;
            }
        }

        view.DeleteCounter(counterLayout, deleteParent);
    }

    @Override
    public void OnCounterTap(String identifier) {
        view.LoadCounterEditDialog(GetPlayerByCounterIdentifier(identifier), GetCounterByIdentifier(identifier));
    }

    @Override
    public void OnCounterLongTap(LinearLayout counterLayout) {
        view.LoadCounterDeletionDialog(counterLayout);
    }

    @Override
    public void OnCounterEditCompleted(PlayerID playerID, String oldCounterIdentifier, Counter newCounter) {
        newCounter.SetIdentifier(oldCounterIdentifier);
        switch (playerID) {
            case ONE:
                players.get(0).UpdateCounter(newCounter);
                view.UpdateCounterView(players.get(0), newCounter);
                break;
            case TWO:
                players.get(1).UpdateCounter(newCounter);
                view.UpdateCounterView(players.get(1), newCounter);
                break;
            case THREE:
                players.get(2).UpdateCounter(newCounter);
                view.UpdateCounterView(players.get(2), newCounter);
                break;
            case FOUR:
                players.get(3).UpdateCounter(newCounter);
                view.UpdateCounterView(players.get(3), newCounter);
                break;
            default:
                break;
        }
    }

    private Counter GetCounterByIdentifier(String identifier) {
        Player currentPlayer;

        switch (PlayerID.valueOf((identifier.split("_"))[0])) {
            case ONE:
                currentPlayer = players.get(0);
                break;
            case TWO:
                currentPlayer = players.get(1);
                break;
            case THREE:
                currentPlayer = players.get(2);
                break;
            case FOUR:
                currentPlayer = players.get(3);
                break;
            default:
                currentPlayer = null;
        }

        if (currentPlayer != null) {
            for (Counter c : currentPlayer.GetAllCounters()) {
                if (c.GetIdentifier() == identifier) {
                    return c;
                }
            }
        }

        return null;
    }

    private Player GetPlayerByCounterIdentifier(String identifier) {
        switch (PlayerID.valueOf((identifier.split("_"))[0])) {
            case ONE:
                return players.get(0);
            case TWO:
                return players.get(1);
            case THREE:
                return players.get(2);
            case FOUR:
                return players.get(3);
            default:
                return null;
        }
    }
}
