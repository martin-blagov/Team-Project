# Fantasy Premier League (FPL) App

This application is a comprehensive tool for building, managing and optimizing teams for FPL.

## Project Summary

The FPL App allows users to enter a team, view detailed player statistics, and receive algorithm 
based recommendations for team improvements. These suggestions include the best team based on given budget, 
transfer suggestions, player risk assessments, and starting lineup selection. 

## Features & User Stories

### 1. Team entry (Elena Ding)

User story: As a user, I want to be able to feed my team into the model.

- Main flow:
  - User clicks on an empty field to enter a player + selects player’s name from side panel
  - System fills text filed with player’s name
  - User confirms team after entering all players
  - System checks for duplicates, budget, valid player/position combo
  - System saves teams and feeds it into other parts of the model
- Alternative flows:
  - User enters a non-valid entry eg. mismatches a player’s position or enters a duplicate
  - System outputs an error and prompts user to enter again

### 2. Generate optimal team (Jieun Park)

User story: As a user, I want to know what the probable best team is within the given budget.

- User clicks Best Team button
- System retrieves player data from the FPL API, including costs, positions, and performance stats.
  - If the system cannot retrieve data from the API, an error message is displayed and the user is prompted to retry.
- System runs an optimization algorithm to select the best combination of 15 players within the £100 million budget.
- System ensures all FPL rules are followed (e.g., max 3 players per club, valid formation).
- System displays the probable best team, including player names, positions, total cost, predicted points, and budget used.

### 3. Player risk assessment (Martin Blagov)

User story: As a user, I want to know what player I should replace in my team.

- User selects **Assess Risks** from the UI.
- System identifies players with risk factors (injury, rotation, form, minutes uncertainty).
- Outputs a simplified **Risk View** displaying only risky players.
- User can click a player to see a detailed explanation of their risk.

### 4. Transfer suggestions (Sachit Sapra)

User story: As a user, I want to the app to suggest transfers to me based on the number of transfers I want to make so that I can improve my FPL team.

- Main Flow:
  - User navigates to Transfer Suggestions from home page
  - System loads and displays user's current team
  - User selects number of transfers (0-15)
  - User clicks "Suggest Transfers"
  - System identifies worst-performing players and finds optimal replacements
  - System displays original vs. suggested team with swap details and points improvement
- Alternative Flows:
  - No team exists: System displays "No team found. Please create a team first."
  - No valid transfers found: System displays "No valid transfers found within budget and club constraints"

### 5. Starting lineup (Junchen Liu)

User story: As a user, I want to know what the best 11 players in my team are, so that I can select who to start for the game week.

- Main flow:
  - User opens starting lineup page
  - System computes best 11 players based on predicted points
  - System output result and display to user
- Alternative flows:
  - No valid team configured: display empty page

### 6. Display individual player stats (Yunseo Won)

User Story: User wants to view the stats of a specific player including current team members.

- Main flow:
  - System display all available players in the market
  - User can use the filter or search specific player
  - User selects player
  - Selected player data is retrieved
  - Corresponding data is displayed
  - User can filter data based on the needs (Total, Average, Last 3 Games, Last 5 Games)
  - All fields reset when going back home

## API Usage

Endpoint: https://fantasy.premierleague.com/api/bootstrap-static/ \
Endpoint: https://fantasy.premierleague.com/api/event/{gw_id}/live/