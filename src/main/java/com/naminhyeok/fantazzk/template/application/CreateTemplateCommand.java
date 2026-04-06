package com.naminhyeok.fantazzk.template.application;

import com.naminhyeok.fantazzk.template.domain.DraftOrderStrategy;
import com.naminhyeok.fantazzk.template.domain.Template;
import java.util.List;
import java.util.Objects;

public sealed interface CreateTemplateCommand
    permits CreateTemplateCommand.Auction, CreateTemplateCommand.Draft {
    String getName();

    int getTeamCount();

    int getTeamSize();

    List<String> getPlayerNames();

    final class Auction implements CreateTemplateCommand {
        private final String name;
        private final int teamCount;
        private final int teamSize;
        private final int budget;
        private final List<String> playerNames;

        public Auction(
            String name,
            int teamCount,
            int teamSize,
            int budget,
            List<String> playerNames
        ) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.teamCount = teamCount;
            this.teamSize = teamSize;
            this.budget = budget;
            this.playerNames = List.copyOf(Objects.requireNonNull(playerNames, "playerNames must not be null"));
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getTeamCount() {
            return teamCount;
        }

        @Override
        public int getTeamSize() {
            return teamSize;
        }

        public int getBudget() {
            return budget;
        }

        @Override
        public List<String> getPlayerNames() {
            return playerNames;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Auction that)) {
                return false;
            }

            return teamCount == that.teamCount
                && teamSize == that.teamSize
                && budget == that.budget
                && name.equals(that.name)
                && playerNames.equals(that.playerNames);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + teamCount;
            result = 31 * result + teamSize;
            result = 31 * result + budget;
            result = 31 * result + playerNames.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "CreateTemplateCommand.Auction("
                + "name="
                + name
                + ", teamCount="
                + teamCount
                + ", teamSize="
                + teamSize
                + ", budget="
                + budget
                + ", playerNames="
                + playerNames
                + ")";
        }
    }

    final class Draft implements CreateTemplateCommand {
        private final String name;
        private final int teamCount;
        private final int teamSize;
        private final DraftOrderStrategy strategy;
        private final List<String> playerNames;

        public Draft(
            String name,
            int teamCount,
            int teamSize,
            DraftOrderStrategy strategy,
            List<String> playerNames
        ) {
            this.name = Objects.requireNonNull(name, "name must not be null");
            this.teamCount = teamCount;
            this.teamSize = teamSize;
            this.strategy = Objects.requireNonNull(strategy, "strategy must not be null");
            this.playerNames = List.copyOf(Objects.requireNonNull(playerNames, "playerNames must not be null"));
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getTeamCount() {
            return teamCount;
        }

        @Override
        public int getTeamSize() {
            return teamSize;
        }

        public DraftOrderStrategy getStrategy() {
            return strategy;
        }

        @Override
        public List<String> getPlayerNames() {
            return playerNames;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Draft that)) {
                return false;
            }

            return teamCount == that.teamCount
                && teamSize == that.teamSize
                && name.equals(that.name)
                && strategy == that.strategy
                && playerNames.equals(that.playerNames);
        }

        @Override
        public int hashCode() {
            int result = name.hashCode();
            result = 31 * result + teamCount;
            result = 31 * result + teamSize;
            result = 31 * result + strategy.hashCode();
            result = 31 * result + playerNames.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "CreateTemplateCommand.Draft("
                + "name="
                + name
                + ", teamCount="
                + teamCount
                + ", teamSize="
                + teamSize
                + ", strategy="
                + strategy
                + ", playerNames="
                + playerNames
                + ")";
        }
    }
}
