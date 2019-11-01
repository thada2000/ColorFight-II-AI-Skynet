import colorfightII.*;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
//import java.util.Math;

public class skynet {
    public static void main(String[] args) {
        /*
         * example ai for java
         */

        // Create a Colorfight Instance. This will be the object that you interact
        // with.
        Colorfight game = new Colorfight();
        /*
        try {
            JSONArray room_list = game.get_gameroom_list();
            ArrayList<JSONObject> rank_rooms = new ArrayList();

            // find all available rank rooms.
            for ( Object o : room_list ) {
                JSONObject room = (JSONObject) o;
                if ( ((Boolean) room.get( "rank" )) && ( (Long) room.get( "player_number" ) < (Long) room.get( "max_player" ) ) ) {
                    rank_rooms.add( room );
                }
            }

            // choose a random available rank room.
            String room = (String) rank_rooms.get( new Random().nextInt(rank_rooms.size()) ).get("name");
            */

            //========================================================================================================

            // delete the line below and uncomment the try-catch block if you want to select a random rank room.
            String room = "official_final";

            //========================================================================================================

            // play game once
            play_game(game, room, "Skynet", "123123123");

            //========================================================================================================

            // run my bot forever
//            while ( true ) {
//                try {
//                    play_game(game, room, "Skynet", "123123123");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }

            //========================================================================================================
            /*
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        */
    }

    private static void play_game(Colorfight game, String room, String username, String password) {
        // Include all the code in a try-catch to handle exceptions.
        // This would help you debug your program.
        try {
            // Connect to the server. This will connect to the public room. If you want to
            // join other rooms, you need to change the argument.

            game.connect( room );
            //String username = "ExampleAI"+( new Random().nextInt(100)+1 );
            //String password = "ExampleAI"+( new Random().nextInt(100)+1 );

            // game.register should return True if succeed.
            // As no duplicate usernames are allowed, a random integer string is appended
            // to the example username. You don't need to do this, change the username
            // to your ID.
            // You need to set a password. For the example AI, a random password is used
            // as the password. You should change it to something that will not change
            // between runs so you can continue the game if disconnected.
            if ( !game.register( username, password ) ) {
                return;
            }

            // The command list we will send to the server
            ArrayList<String> cmd_list = new ArrayList<>();
            // The list of cells that we want to attack
            ArrayList<Position> my_attack_list = new ArrayList<>();
            // The list of cells that we want to build a fortress on
            ArrayList<MapCell> my_defend_list = new ArrayList<>();
            
            // The previous list of cells that we want to build a fortress on
            ArrayList<MapCell> prev_my_defend_list = new ArrayList<>();
            
            // This is the game loop
            while( true ){
                // empty the two lists in the start of each round.
                prev_my_defend_list = (ArrayList<MapCell>) my_defend_list.clone();
                
                cmd_list = new ArrayList<>();
                my_attack_list = new ArrayList<>();
                my_defend_list = new ArrayList<>();
                
                // update_turn() is required to get the latest information from the
                // server. This will halt the program until it receives the updated
                // information.
                // After update_turn(), game object will be updated.
                if ( !game.update_turn() ) {
                    break;
                }

                // Check if you exist in the game. If not, wait for the next round.
                // You may not appear immediately after you join. But you should be
                // in the game after one round.
                if (game.me==null) continue;

                User me = game.me;
                MapCell home = null;
                int gold_allowance = me.gold;
                if ( me.cells.size() > 225 )
                    gold_allowance =  me.gold_source;
                
                Collections.shuffle( game.me.cells );
                // game.me.cells is a Arraylist of MapCells.
                // The outer loop gets all my cells.
                for ( MapCell cell:game.me.cells ) {
                    if ( cell.is_home )
                        home = cell;
                    
                    // The inner loop checks the surrounding positions.
                    for ( Position pos:cell.position.get_surrounding_cardinals() ) {
                        // Get the MapCell object of that position
                        MapCell c = game.game_map.get_cell( pos );

                        // Attack if the cost is less than what I have, and the owner
                        // is not mine, and I have not attacked it in this round already
                        if ( c.owner !=  me.uid ) {
                            if ( c.owner != 0 && !my_defend_list.contains( cell ) ) {
                                int defend_energy = (int) ( me.energy_source / ( 0.75 * prev_my_defend_list.size() + 1) );
                                if ( ( defend_energy < me.energy ) &&
                                    !my_defend_list.contains( cell ) && game.turn > 50 ) {
                                    cmd_list.add( game.attack( cell.position, defend_energy ) );
                                    game.me.energy -= defend_energy;
                                    System.out.println( "we are attacking {" +
                                                       cell.position.x + "," + cell.position.y +
                                                       "} with " + defend_energy + " energy" );
                                }
                                my_defend_list.add( cell );
                            }
                            
                            int attack_energy = (int) ( 1.25 * c.attack_cost );
                            if ( c.owner == 0 )
                                attack_energy = c.attack_cost;
                            if ( ( attack_energy < me.energy ) &&
                                    ( !my_attack_list.contains( c.position ) ) &&
                                    ( c.natural_energy >= 4 || c.natural_gold >= 4 ) &&
                                    ( me.cells.size() < 226 ) ) {
                                // Add the attack command in the command list
                                // Subtract the attack cost manually so I can keep track
                                // of the energy I have.
                                // Add the position to the attack list so I won't attack
                                // the same cell
                                cmd_list.add( game.attack( pos, attack_energy ) );
                                game.me.energy -= attack_energy;
                                System.out.println( "we are attacking {" +
                                        pos.x + "," + pos.y +
                                        "} with " + attack_energy + " energy" );
                                my_attack_list.add( c.position );
                            }
                            
                            if (  c.owner == 0 && ( c.attack_cost < 0.5 * me.energy ) &&
                                    ( !my_attack_list.contains( c.position ) ) &&
                                    ( c.natural_energy >= 7 || c.natural_gold >= 4 ) &&
                                    ( me.cells.size() > 225 ) && ( me.cells.size() < 451 ) ) {
                                // Add the attack command in the command list
                                // Subtract the attack cost manually so I can keep track
                                // of the energy I have.
                                // Add the position to the attack list so I won't attack
                                // the same cell
                                cmd_list.add( game.attack( pos, c.attack_cost ) );
                                game.me.energy -= c.attack_cost;
                                System.out.println( "we are attacking {" +
                                        pos.x + "," + pos.y +
                                        "} with " + c.attack_cost + " energy" );
                                my_attack_list.add( c.position );
                            }
                        }
                        
                        for ( Position p:pos.get_surrounding_cardinals() ) {
                            // Get the MapCell object of that position
                            MapCell c2 = game.game_map.get_cell( p );
                            if ( c2.owner != me.uid && c2.owner != 0 && !my_defend_list.contains( cell ) )
                                my_defend_list.add( cell );
                        }

                    }
                    
                }
                                
                // home relocation
                if ( home == null ) {
                    for ( MapCell cell:game.me.cells ) {
                        if ( cell.is_empty && me.gold >= 1000 ) {
                            cmd_list.add( game.build( cell.position, Constants.BLD_HOME ) );
                            System.out.println( "we build " + Constants.BLD_HOME +
                                    " on {" + cell.position.x+","+
                                    cell.position.y+"}" );
                            me.gold -= 1000;
                        }
                    }
                }
                
                // home could not be relocated
                if ( home == null ) continue;
                
                // build fortresses
                for ( MapCell cell:my_defend_list ) {
                    if ( ( cell.owner==me.uid ) && ( cell.building.is_empty ) &&
                            ( me.gold >= Constants.BUILDING_COST.getX() ) ) {
                        cmd_list.add( game.build( cell.position, Constants.BLD_FORTRESS ) );
                        System.out.println( "we build " + Constants.BLD_FORTRESS +
                                " on {" + cell.position.x+","+
                                cell.position.y+"}" );
                        me.gold -= 100;
                        break;
                    }
                }

                // upgrade fortresses
                for ( MapCell cell:my_defend_list ) {
                    if ( ( cell.building.name == "fortress" ) &&
                            ( cell.building.can_upgrade ) &&
                            ( cell.building.level < me.tech_level ) &&
                            ( cell.building.upgrade_gold < me.gold ) ) {
                        cmd_list.add( game.upgrade( cell.position ) );
                        System.out.println( "we upgraded {"+cell.position.x+","+cell.position.y+"}" );
                        me.gold -= cell.building.upgrade_gold;
                    }
                }
                
//                // build home defense
//                for ( Position pos:home.position.get_surrounding_cardinals() ) {
//                    MapCell c = game.game_map.get_cell( pos );
//                    if ( ( c.owner==me.uid ) && ( c.building.is_empty ) &&
//                            ( me.gold >= Constants.BUILDING_COST.getX() ) ) {
//                        cmd_list.add( game.build( c.position, Constants.BLD_FORTRESS ) );
//                        System.out.println( "we build " + Constants.BLD_FORTRESS +
//                                " on {" + c.position.x+","+
//                                c.position.y+"}" );
//                        me.gold -= 100;
//                    }
//                }
//
//                // upgrade home defense
//                for ( Position pos:home.position.get_surrounding_cardinals() ) {
//                    MapCell c = game.game_map.get_cell( pos );
//                    if ( ( c.building.can_upgrade ) &&
//                            ( c.building.level < me.tech_level ) &&
//                            ( c.building.upgrade_gold < gold_allowance ) ) {
//                        cmd_list.add( game.upgrade( c.position ) );
//                        System.out.println( "we upgraded {"+c.position.x+","+c.position.y+"}" );
//                        me.gold -= c.building.upgrade_gold;
//                    }
//                }
                
                if ( game.max_turn - game.turn < 50 )
                    continue;
                
                // upgrade home
                if ( (home.building.can_upgrade) &&
                    ( home.building.upgrade_gold < me.gold ) &&
                    ( home.building.upgrade_energy < me.energy ) ) {
                    cmd_list.add( game.upgrade( home.position ) );
                    System.out.println( "we upgraded {"+home.position.x+","+home.position.y+"}" );
                    me.gold -= home.building.upgrade_gold;
                    me.energy -= home.building.upgrade_energy;
                }
                
                // build energy wells/gold mines
                for ( MapCell cell:game.me.cells ) {
                    if ( ( cell.owner==me.uid ) && ( cell.building.is_empty ) &&
                            ( gold_allowance >= Constants.BUILDING_COST.getX() ) ) {
                        char building = 0;
                        
                        if ( Math.max(cell.natural_energy, cell.natural_gold) >= 9 ) {
                            if ( cell.natural_energy >= cell.natural_gold )
                                building = Constants.BLD_ENERGY_WELL;
                            else
                                building = Constants.BLD_GOLD_MINE;
                        }
                        else if ( me.cells.size() < 51 ) {
                            if ( cell.natural_energy >= 7 )
                                building = Constants.BLD_ENERGY_WELL;
                            else if ( cell.natural_gold >= 7 )
                                building = Constants.BLD_GOLD_MINE;
                            else
                                building = Constants.BLD_ENERGY_WELL;
                        }
                        else if ( me.cells.size() > 50 && me.cells.size() < 226 ) {
                            if ( Math.max(cell.natural_energy, cell.natural_gold) >= 7 ) {
                                if ( cell.natural_energy >= cell.natural_gold )
                                    building = Constants.BLD_ENERGY_WELL;
                                else
                                    building = Constants.BLD_GOLD_MINE;
                            }
                        }
                        else if ( me.cells.size() > 225 ) {
                            if ( cell.natural_gold >= 7 )
                                building = Constants.BLD_GOLD_MINE;
                            else if ( cell.natural_energy >= 7 )
                                building = Constants.BLD_ENERGY_WELL;
                            else
                                building = Constants.BLD_GOLD_MINE;
                        }
                        else
                            continue;
//                        else {
//                            if ( new Random().nextFloat() < 0.5 ) {
//                                if ( cell.natural_energy >= cell.natural_gold )
//                                    building = Constants.BLD_ENERGY_WELL;
//                                else
//                                    building = Constants.BLD_GOLD_MINE;
//                            }
//                            else
//                                break;
//                        }
                        cmd_list.add( game.build( cell.position, building ) );
                        System.out.println( "we build " + building +
                                " on {" + cell.position.x+","+
                                cell.position.y+"}" );
                        me.gold -= 100;
                    }
                }
                
                // upgrade energy wells/gold mines
                for ( MapCell cell:game.me.cells ) {
                    if ( ( cell.building.can_upgrade ) &&
                            ( cell.building.level < me.tech_level ) &&
                            ( cell.building.upgrade_gold < gold_allowance ) ) {
                        cmd_list.add( game.upgrade( cell.position ) );
                        System.out.println( "we upgraded {"+cell.position.x+","+cell.position.y+"}" );
                        me.gold -= cell.building.upgrade_gold;
                    }
                }
                
                // Send the command list to the server
                // and print out the message from server
                System.out.println( game.send_cmd( cmd_list ).toString() );
            }
            game.disconnect();
        } catch ( URISyntaxException e ) {
            e.printStackTrace();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        } catch ( ParseException e ) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
