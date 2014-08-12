/*
 * $Id: base-templates.groovy 2215 2012-05-07 23:50:13Z pspeed $
 *
 * Copyright (c) 2012, Paul Speed
 * All rights reserved.
 */


import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;

import mythruna.Direction;
import mythruna.ShapeIndex;
import mythruna.phys.collision.*;


blockLibrary( name:"mythruna.base-templates" ) {

    addTemplate( "wedge-n" ) {
        collider( new WedgeCollider( Direction.NORTH, true ) );
        
        volume( 0.5 );
        
        north( ShapeIndex.UNIT_SQUARE ) {
            
            quad {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.UNIT_SQUARE ) {
            quad {
                material(0);
                
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 1 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
            }
        }

        east( ShapeIndex.getTriangle( 0, 0, 1, 1, FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(0.5, 0.5, -0.5) {
                    texture(0,1);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(0.5, -0.5, 0.5) {
                    texture(1,1);
                } 
            }         
        }
 
        west( ShapeIndex.getTriangle( 0, 0, 1, 1, FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(1,1);
                }
                
                vertex(-0.5, -0.5, 0.5) {
                    texture(0,1);
                } 
            }         
        }
        
        internal {
            quad {
                material(100);
                
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
                allNormals( 0, 1, 1 ).normalizeLocal();
                allTangents( 1, 0, 0 );
            }
        }
    }

    addTemplate( "wedge-s" ) {
        fromTemplate( "wedge-n" ) {
            rotate(180);
        }
        
        /*down( ShapeIndex.UNIT_SQUARE ) {
            clear();
            quad {
                material(0);
                
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 1 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
            }
        }*/
    }
    addTemplate( "wedge-e" ) {
        fromTemplate( "wedge-n" ) {
            rotate(90);
        }
        
        /*down( ShapeIndex.UNIT_SQUARE ) {
            clear();
            quad {
                material(0);
                
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 1 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
            }
        }*/
    }
    addTemplate( "wedge-w" ) {
        fromTemplate( "wedge-n" ) {
            rotate(-90);
        }
        
        /*down( ShapeIndex.UNIT_SQUARE ) {
            clear();
            quad {
                material(0);
                
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 1 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
            }
        }*/
    }


    addTemplate( "wedge-up-n" ) {
        collider( new WedgeCollider( Direction.NORTH, false ) );
        volume( 0.5 );
        
        north( ShapeIndex.UNIT_SQUARE ) {
            
            triangle {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
            }
            
            triangle {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        up( ShapeIndex.UNIT_SQUARE ) {
            quad {
                material(0);
                
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, 0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }

        east( ShapeIndex.getTriangle( 0, 0, 1, 1, FastMath.sqrt(2), -FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(0.5, 0.5, 0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(0.5, -0.5, 0.5) {
                    texture(1,1);
                } 
            }         
        }
 
        west( ShapeIndex.getTriangle( 0, 0, 1, 1, FastMath.sqrt(2), -FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0.5, 0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, -0.5, 0.5) {
                    texture(0,1);
                } 
            }         
        }
        
        internal {
            quad {
                material(100);
                
                vertex( 0.5, 0.5, 0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 1 );
                }
                allNormals( 0, 1, -1 ).normalizeLocal();
                allTangents( -1, 0, 0 );
            }
        }
    }

    addTemplate( "wedge-up-s" ) {
        fromTemplate( "wedge-up-n" ) {
            rotate(180);
        }
        
        up( ShapeIndex.UNIT_SQUARE ) {
            clear();
            quad {
                material(0);
                
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, 0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
    }
    addTemplate( "wedge-up-e" ) {
        fromTemplate( "wedge-up-n" ) {
            rotate(90);
        }
        
        up( ShapeIndex.UNIT_SQUARE ) {
            clear();
            quad {
                material(0);
                
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, 0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
    }
    addTemplate( "wedge-up-w" ) {
        fromTemplate( "wedge-up-n" ) {
            rotate(-90);
        }
        
        up( ShapeIndex.UNIT_SQUARE ) {
            clear();
            quad {
                material(0);
                
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, 0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
    }


    addTemplate( "half-wedge-n" ) {

        // An approximation of the collider for now
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 1, 0.5f );              
        collider( new CubeCollider( oMin, oMax, "half-wedge" ) );
        
        volume( 0.25 );
        //collider( new WedgeCollider( Direction.NORTH, true ) );
        
        north( ShapeIndex.getRect(0, 0, 1, 0.5) ) {
            
            quad {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, 0 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, 0 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.UNIT_SQUARE ) {
            quad {
                material(0);
                
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 1 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
            }
        }

        Vector2f normal = new Vector2f( 0.5f, 1f ).normalizeLocal();
        east( ShapeIndex.getTriangle( 0, 0, 1, 0.5f, normal.x, normal.y ) ) {
            triangle {
                material(0);
                
                vertex(0.5, 0.5, -0.5) {
                    texture(0,1);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(0.5, -0.5, 0) {
                    texture(1,1);
                } 
            }         
        }
 
        west( ShapeIndex.getTriangle( 0, 0, 1, 0.5f, normal.x, normal.y ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(1,1);
                }
                
                vertex(-0.5, -0.5, 0) {
                    texture(0,1);
                } 
            }         
        }
        
        internal {
            quad {
                material(100);
                
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0 ) {
                    texture( 0, 1 );
                }
                allNormals( 0, 0.5f, 1 ).normalizeLocal();
                allTangents( 1, 0, 0 );
            }
        }
    }
    addTemplate( "half-wedge-s" ) {
        fromTemplate( "half-wedge-n" ) {
            rotate(180);
        }
    }
    addTemplate( "half-wedge-e" ) {
        fromTemplate( "half-wedge-n" ) {
            rotate(90);
        }
    }
    addTemplate( "half-wedge-w" ) {
        fromTemplate( "half-wedge-n" ) {
            rotate(-90);
        }
    }

    addTemplate( "half-corner-ne" ) {

        // An approximation of the collider for now
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 1, 0.5f );              
        collider( new CubeCollider( oMin, oMax, "half-corner" ) );
 
        volume( 0.125 );
       
        //collider( new WedgeCollider( Direction.NORTH, true ) );

        // This is the rough normal of the triangles at the end caps.
        Vector2f normal = new Vector2f( 0.5f, 1f ).normalizeLocal();

        west( ShapeIndex.getTriangle( 0, 0, 1, 0.5f, -normal.x, normal.y ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,1);
                }
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, 0.5, 0) {
                    texture(1,1);
                } 
            }         
        }

        south( ShapeIndex.getTriangle( 0, 0, 1, 0.5f, normal.x, normal.y ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, 0.5, -0.5) {
                    texture(1,1);
                }
                
                vertex(-0.5, 0.5, 0) {
                    texture(0,1);
                } 
            }         
        }
 
        down( ShapeIndex.getTriangle( 0, 0, 1, 1, FastMath.sqrt(2), -FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, 0.5, -0.5) {
                    texture(1,1);
                }
                
                vertex(-0.5, 0.5, -0.5 ) {
                    texture(0,1);
                }
            }            
        } 
 
        internal {
 
            triangle {
                material(100);
                
                vertex(0.5, 0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,1);
                }
                
                vertex(-0.5, 0.5, 0 ) {
                    texture(0,0);
                }
                
                allNormals(1, -1, 1).normalizeLocal();
                allTangents(1, 0, -0.5 ).normalizeLocal(); 
            }
        }                 
 
    }

    addTemplate( "half-corner-nw" ) {
        fromTemplate( "half-corner-ne" ) {
            rotate(-90);
        }
    }
    addTemplate( "half-corner-se" ) {
        fromTemplate( "half-corner-ne" ) {
            rotate(90);
        }
    }
    addTemplate( "half-corner-sw" ) {
        fromTemplate( "half-corner-ne" ) {
            rotate(180);
        }
    }


    addTemplate( "half-wedge-up-n" ) {

        // An approximation of the collider for now
        Vector3f oMin = new Vector3f( 0, 0, 0f );
        Vector3f oMax = new Vector3f( 1, 1, 1 );              
        //collider( new CubeCollider( oMin, oMax ) );
        collider( new CubeCollider("half-wedge-up") );
        
        volume( 0.125 + 0.5 );
        
        //collider( new WedgeCollider( Direction.NORTH, true ) );
        
        north( ShapeIndex.UNIT_SQUARE ) {
            
            quad {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }

        south( ShapeIndex.getRect(0, 0, 1, 0.5) ) {
            
            quad {
                material(0);
                
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, 0.5, 0 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, 0 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.UNIT_SQUARE ) {
            quad {
                material(0);
                
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 1 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
            }
        }

        // We have no shape representing a triangle on top of
        // a block.... so we will pick something in between
        // just to have a match.
        east( ShapeIndex.getTriangle(0, 0.5, 1, 1, FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(0.5, 0.5, 0) {
                    texture(0,1);
                }
                
                vertex(0.5, -0.5, 0) {
                    texture(1,0);
                }
                
                vertex(0.5, -0.5, 0.5) {
                    texture(1,1);
                } 
            }
                     
            triangle {
                material(0);
                
                vertex(0.5, 0.5, -0.5) {
                    texture(0,1);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(0.5, -0.5, 0) {
                    texture(1,1);
                } 
            }
                     
            triangle {
                material(0);
                
                vertex(0.5, 0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, -0.5, 0) {
                    texture(1,0);
                }
                
                vertex(0.5, 0.5, 0) {
                    texture(0,1);
                } 
            }         
        }
 
        west( ShapeIndex.getTriangle(0, 0.5, 1, 1, FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, 0) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0.5, 0) {
                    texture(1,1);
                }
                
                vertex(-0.5, -0.5, 0.5) {
                    texture(0,1);
                } 
            }
                     
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(1,1);
                }
                
                vertex(-0.5, -0.5, 0) {
                    texture(0,1);
                } 
            }
                     
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, 0) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, 0.5, 0) {
                    texture(0,1);
                } 
            }         
        }
        
        internal {
            quad {
                material(100);
                
                vertex( -0.5, 0.5, 0 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, 0 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
                allNormals( 0, 0.5f, 1 ).normalizeLocal();
                allTangents( 1, 0, 0 );
            }
        }
    }
    addTemplate( "half-wedge-up-s" ) {
        fromTemplate( "half-wedge-up-n" ) {
            rotate(180);
        }
    }
    addTemplate( "half-wedge-up-e" ) {
        fromTemplate( "half-wedge-up-n" ) {
            rotate(90);
        }
    }
    addTemplate( "half-wedge-up-w" ) {
        fromTemplate( "half-wedge-up-n" ) {
            rotate(-90);
        }
    }


    addTemplate( "hi-wedge-n" ) {

        // An approximation of the collider for now
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 0.5, 1 );              
        collider( new CubeCollider( oMin, oMax, "hi-wedge" ) );
        
        volume( 0.25 );
        //collider( new WedgeCollider( Direction.NORTH, true ) );
        
        north( ShapeIndex.UNIT_SQUARE ) {
            
            quad {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.getRect(0, 0, 1, 0.5) ) {
            quad {
                material(0);
                
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, 0, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0, -0.5 ) {
                    texture( 0, 1 );
                }
            }
        }

        Vector2f normal = new Vector2f( 1, 0.5f ).normalizeLocal();
        east( ShapeIndex.getTriangle( 0, 0, 0.5f, 1, normal.x, normal.y ) ) {
            triangle {
                material(0);
                
                vertex(0.5, 0, -0.5) {
                    texture(0.5,1);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0.5);
                }
                
                vertex(0.5, -0.5, 0.5) {
                    texture(1,1);
                } 
            }
                     
        }
 
        west( ShapeIndex.getTriangle( 0, 0, 0.5f, 1, normal.x, normal.y ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0.5);
                }
                
                vertex(-0.5, 0, -0.5) {
                    texture(0.5,1);
                }
                
                vertex(-0.5, -0.5, 0.5) {
                    texture(0,1);
                } 
            }
                     
        }
        
        internal {
            quad {
                material(100);
                
                vertex( -0.5, 0, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
                allNormals( 0, 1, 0.5f ).normalizeLocal();
                allTangents( 1, 0, 0 );
            }
        }
    }
    addTemplate( "hi-wedge-s" ) {
        fromTemplate( "hi-wedge-n" ) {
            rotate(180);
        }
    }
    addTemplate( "hi-wedge-e" ) {
        fromTemplate( "hi-wedge-n" ) {
            rotate(90);
        }
    }
    addTemplate( "hi-wedge-w" ) {
        fromTemplate( "hi-wedge-n" ) {
            rotate(-90);
        }
    }

    addTemplate( "hi-wedge-wide-n" ) {

        // An approximation of the collider for now
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 1, 1 );              
        collider( new CubeCollider( oMin, oMax, "hi-wedge-wide" ) );
        
        volume( 0.75 );
        //collider( new WedgeCollider( Direction.NORTH, true ) );
        
        north( ShapeIndex.UNIT_SQUARE ) {
            
            quad {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        up( ShapeIndex.getRect(0, 0, 1, 0.5) ) {
            quad {
                material(0);
                
                vertex( -0.5, 0, 0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0, 0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.UNIT_SQUARE ) {  //ShapeIndex.getRect(0, 0.5, 1, 1)
            quad {
                material(0);
                
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 1 );
                }
            }
        }

        Vector2f normal = new Vector2f( 1, 0.5f ).normalizeLocal();
        
        east( ShapeIndex.getRect(0, 0, 0.75, 1) ) {
            triangle {
                material(0);
                
                vertex(0.5, 0.5, -0.5) {
                    texture(0,1);
                }
                
                vertex(0.5, 0, -0.5) {
                    texture(0.5,0.5);
                }
                
                vertex(0.5, 0, 0.5) {
                    texture(0.5,1);
                } 
            }         
            triangle {
                material(0);
                
                vertex(0.5, 0, -0.5) {
                    texture(0.5,0.5);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(0.5, -0.5, 0.5) {
                    texture(1,0.5);
                } 
            }         
            triangle {
                material(0);
                
                vertex(0.5, 0, -0.5) {
                    texture(0.5,0.5);
                }
                
                vertex(0.5, -0.5, 0.5) {
                    texture(1,0.5);
                }
                
                vertex(0.5, 0, 0.5) {
                    texture(0.5,1);
                } 
            }         
        }
 
        west( ShapeIndex.getRect(0, 0, 0.75, 1) ) {
            triangle {
                material(0);
                
                vertex(-0.5, 0, -0.5) {
                    texture(0.5,0.5);
                }
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(1,1);
                }
                
                vertex(-0.5, 0, 0.5) {
                    texture(0.5,1);
                } 
            }         
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0, -0.5) {
                    texture(0.5,0.5);
                }
                
                vertex(-0.5, -0.5, 0.5) {
                    texture(0,0.5);
                } 
            }         
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, 0.5) {
                    texture(0,0.5);
                }
                
                vertex(-0.5, 0, -0.5) {
                    texture(0.5,0.5);
                }
                
                vertex(-0.5, 0, 0.5) {
                    texture(0.5,1);
                } 
            }         
        }
        
        internal {
            quad {
                material(100);
                
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, 0, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0, 0.5 ) {
                    texture( 0, 1 );
                }
                allNormals( 0, 1, 0.5f ).normalizeLocal();
                allTangents( 1, 0, 0 );
            }
        }
    }
    addTemplate( "hi-wedge-wide-s" ) {
        fromTemplate( "hi-wedge-wide-n" ) {
            rotate(180);
        }
    }
    addTemplate( "hi-wedge-wide-e" ) {
        fromTemplate( "hi-wedge-wide-n" ) {
            rotate(90);
        }
    }
    addTemplate( "hi-wedge-wide-w" ) {
        fromTemplate( "hi-wedge-wide-n" ) {
            rotate(-90);
        }
    }


    addTemplate( "hi-wedge-up-n" ) {

        // An approximation of the collider for now
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 0.5, 1 );              
        collider( new CubeCollider( oMin, oMax, "hi-wedge-up" ) );
        
        volume( 0.25 );
        //collider( new WedgeCollider( Direction.NORTH, true ) );
        
        north( ShapeIndex.UNIT_SQUARE ) {
            
            quad {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        up( ShapeIndex.getRect(0, 0, 1, 0.5) ) {
            quad {
                material(0);
                
                vertex( -0.5, 0, 0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0, 0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }

        Vector2f normal = new Vector2f( 1, -0.5f ).normalizeLocal();
        east( ShapeIndex.getTriangle( 0, 0, 0.5f, 1, normal.x, normal.y ) ) {
            triangle {
                material(0);
                
                vertex(0.5, 0, 0.5) {
                    texture(0.5,0);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(0.5, -0.5, 0.5) {
                    texture(1,0.5); 
                } 
            }
                     
        }
 
        west( ShapeIndex.getTriangle( 0, 0, 0.5f, 1, normal.x, normal.y ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0, 0.5) {
                    texture(0.5,0);
                }
                
                vertex(-0.5, -0.5, 0.5) {
                    texture(0,0.5);
                } 
            }
                     
        }
        
        internal {
            quad {
                material(100);
                
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, 0, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0, 0.5 ) {
                    texture( 0, 1 );
                }
                allNormals( 0, 1, -0.5f ).normalizeLocal();
                allTangents( 1, 0, 0 );
            }
        }
    }
    addTemplate( "hi-wedge-up-s" ) {
        fromTemplate( "hi-wedge-up-n" ) {
            rotate(180);
        }
    }
    addTemplate( "hi-wedge-up-e" ) {
        fromTemplate( "hi-wedge-up-n" ) {
            rotate(90);
        }
    }
    addTemplate( "hi-wedge-up-w" ) {
        fromTemplate( "hi-wedge-up-n" ) {
            rotate(-90);
        }
    }


    addTemplate( "hi-wedge-wide-up-n" ) {

        // An approximation of the collider for now
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 1, 1 );              
        collider( new CubeCollider( oMin, oMax, "hi-wedge-wide-up" ) );
        
        volume( 0.75 );
        //collider( new WedgeCollider( Direction.NORTH, true ) );
        
        north( ShapeIndex.UNIT_SQUARE ) {
            
            quad {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        up( ShapeIndex.UNIT_SQUARE ) {
            quad {
                material(0);
                
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, 0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.getRect(0, 0, 1, 0.5) ) {  //ShapeIndex.getRect(0, 0.5, 1, 1)
            quad {
                material(0);
                
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, 0, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0, -0.5 ) {
                    texture( 0, 1 );
                }
            }
        }

        Vector2f normal = new Vector2f( 1, 0.5f ).normalizeLocal();
        
        east( ShapeIndex.getRect(0, 0, 0.75, 1) ) {
            triangle {
                material(0);
                
                vertex(0.5, 0.5, 0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, 0, -0.5) {
                    texture(0.5,0);
                }
                
                vertex(0.5, 0, 0.5) {
                    texture(0.5,0.5);
                } 
            }         
            triangle {
                material(0);
                
                vertex(0.5, 0, 0.5) {
                    texture(0.5,0.5);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0.5);
                }
                
                vertex(0.5, -0.5, 0.5) {
                    texture(1,1);
                } 
            }         
            triangle {
                material(0);
                
                vertex(0.5, 0, -0.5) {
                    texture(0.5,0);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0.5);
                }
                
                vertex(0.5, 0, 0.5) {
                    texture(0.5,0.5);
                } 
            }         
        }
 
        west( ShapeIndex.getRect(0, 0, 0.75, 1) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0.5);
                }
                
                vertex(-0.5, 0, 0.5) {
                    texture(0.5,0.5);
                }
                
                vertex(-0.5, -0.5, 0.5) {
                    texture(0,1);
                } 
            }         
            triangle {
                material(0);
                
                vertex(-0.5, 0, -0.5) {
                    texture(0.5,0);
                }
                
                vertex(-0.5, 0.5, 0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, 0, 0.5) {
                    texture(0.5,0.5);
                } 
            }         
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0.5);
                }
                
                vertex(-0.5, 0, -0.5) {
                    texture(0.5,0);
                }
                
                vertex(-0.5, 0, 0.5) {
                    texture(0.5,0.5);
                } 
            }         
        }
        
        internal {
            quad {
                material(100);
                
                vertex( -0.5, 0, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, 0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 0, 1 );
                }
                allNormals( 0, 1, -0.5f ).normalizeLocal();
                allTangents( 1, 0, 0 );
            }
        }
    }
    addTemplate( "hi-wedge-wide-up-s" ) {
        fromTemplate( "hi-wedge-wide-up-n" ) {
            rotate(180);
        }
    }
    addTemplate( "hi-wedge-wide-up-e" ) {
        fromTemplate( "hi-wedge-wide-up-n" ) {
            rotate(90);
        }
    }
    addTemplate( "hi-wedge-wide-up-w" ) {
        fromTemplate( "hi-wedge-wide-up-n" ) {
            rotate(-90);
        }
    }

    addTemplate( "shallow-corner-ne" ) {
        
        // An approximation of the collider for now
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 1, 0.5 );              
        collider( new CubeCollider( oMin, oMax, "shallow-corner" ) );
        
        volume( 0.5 );

        // North and east are just half-triangles

        Vector2f normal = new Vector2f( 0.5f, 1f ).normalizeLocal();
        north( ShapeIndex.getTriangle( 0, 0, 1, 0.5f, normal.x, normal.y ) ) {
            triangle {
                material(0);
                
                vertex(0.5, -0.5, -0.5) {
                    texture(0,1);
                }
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, -0.5, 0) {
                    texture(1,1);
                } 
            }
                     
        }
 
        east( ShapeIndex.getTriangle( 0, 0, 1, 0.5f, -normal.x, normal.y ) ) {
        
            triangle {
                material(0);
                
                vertex(0.5, 0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,1);
                }
                
                vertex(0.5, 0.5, 0) {
                    texture(0,1);
                } 
            }
        }        

        // Taller sides are west and south
        
        // We approximate the shape because we don't have
        // one that matches a triangle on top of a quad.
        west( ShapeIndex.getTriangle(0, 0.5, 1, 1, -FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, 0) {
                    texture(0,1);
                }
                
                vertex(-0.5, 0.5, 0) {
                    texture(1, 0.5); 
                }
                
                vertex(-0.5, 0.5, 0.5) {
                    texture(1,1);
                }
            }
            
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(1, 0); 
                }
                
                vertex(-0.5, 0.5, 0) {
                    texture(1,0.5);
                }
            }
            
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(-0.5, 0.5, 0) {
                    texture(1, 0.5); 
                }
                
                vertex(-0.5, -0.5, 0) {
                    texture(0,1);
                }
            }
        }
        
        // We approximate the shape because we don't have
        // one that matches a triangle on top of a quad.
        south( ShapeIndex.getTriangle(0, 0.5, 1, 1, FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, 0.5, 0) {
                    texture(0,0.5);
                }
                
                vertex(0.5, 0.5, 0) {
                    texture(1, 1); 
                }
                
                vertex(-0.5, 0.5, 0.5) {
                    texture(0,1);
                }
            }
            
            triangle {
                material(0);
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, 0.5, -0.5) {
                    texture(1, 0); 
                }
                
                vertex(-0.5, 0.5, 0) {
                    texture(0,0.5);
                }
            }
            
            triangle {
                material(0);
                
                vertex(-0.5, 0.5, 0) {
                    texture(0,0.5);
                }
                
                vertex(0.5, 0.5, -0.5) {
                    texture(1, 0); 
                }
                
                vertex(0.5, 0.5, 0) {
                    texture(1,1);
                }
            }
        }
        
        down( ShapeIndex.UNIT_SQUARE ) {  
            quad {
                material(0);
                
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
 
        internal {            
            quad {
                material(100);
                
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, 0.5, 0 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0 ) {
                    texture( 0, 1 );
                }
                allNormals( 1, -1, 1 ).normalizeLocal();
                allTangents( 1, 0, -0.5 ).normalizeLocal();
            }
        }
               
    }
    addTemplate( "shallow-corner-nw" ) {
        fromTemplate( "shallow-corner-ne" ) {
            rotate(-90);
        }
    }
    addTemplate( "shallow-corner-se" ) {
        fromTemplate( "shallow-corner-ne" ) {
            rotate(90);
        }
    }
    addTemplate( "shallow-corner-sw" ) {
        fromTemplate( "shallow-corner-ne" ) {
            rotate(180);
        }
    }

    

    addTemplate( "half-dn" ) {
 
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 1, 0.5f );          
    
        collider( new CubeCollider( oMin, oMax ) );

        Vector3f min = oMin.subtract( 0.5f, 0.5f, 0.5f );
        Vector3f max = oMax.subtract( 0.5f, 0.5f, 0.5f );
        
        north( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( max.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        south( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        west( ShapeIndex.getRect(oMin.y, oMin.z, oMax.y, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        east( ShapeIndex.getRect(oMin.y, oMin.z, oMax.y, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        internal( Direction.UP ) { 
            quad {
                material(0);
                
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 0, 1 );
                }
            }
        }

    }

    addTemplate( "half-up" ) {
 
        Vector3f oMin = new Vector3f( 0, 0, 0.5f );
        Vector3f oMax = new Vector3f( 1, 1, 1 );          
    
        collider( new CubeCollider( oMin, oMax ) );

        Vector3f min = oMin.subtract( 0.5f, 0.5f, 0.5f );
        Vector3f max = oMax.subtract( 0.5f, 0.5f, 0.5f );
        
        north( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( max.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        south( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        west( ShapeIndex.getRect(oMin.y, oMin.z, oMax.y, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        east( ShapeIndex.getRect(oMin.y, oMin.z, oMax.y, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        up( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad {
                material(0);
                
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }
        
        internal( Direction.DOWN ) { //ShapeIndex.getRect(min.x, min.y, max.x, max.y) ) {
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 0, 1 );
                }
            }
        }

    }

    addTemplate( "wall-w" ) {
 
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 0.5f, 1, 1 );          
    
        collider( new CubeCollider( oMin, oMax ) );

        Vector3f min = oMin.subtract( 0.5f, 0.5f, 0.5f );
        Vector3f max = oMax.subtract( 0.5f, 0.5f, 0.5f );
        
        north( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( max.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        south( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        west( ShapeIndex.getRect(oMin.y, oMin.z, oMax.y, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        internal( Direction.EAST ) {
 
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        up( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad {
                material(0);
                
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 0, 1 );
                }
            }
        }

    }
    addTemplate( "wall-e" ) {
        fromTemplate( "wall-w" ) {
            rotate(180);
        }

        Vector3f oMin = new Vector3f( 0.5f, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 1, 1 );          
        Vector3f min = oMin.subtract( 0.5f, 0.5f, 0.5f );
        Vector3f max = oMax.subtract( 0.5f, 0.5f, 0.5f );
                
        up( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad(0) { retexture(Direction.UP) }
        }
        
        down( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad(0) { retexture(Direction.DOWN) }
        }
    }
    addTemplate( "wall-n" ) {
        fromTemplate( "wall-w" ) {
            rotate(90);
        }
         
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 1, 0.5f, 1 );          
        Vector3f min = oMin.subtract( 0.5f, 0.5f, 0.5f );
        Vector3f max = oMax.subtract( 0.5f, 0.5f, 0.5f );
        
        up( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad(0) { retexture(Direction.UP) }
        }
        
        down( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad(0) { retexture(Direction.DOWN) }
        }
    }
    addTemplate( "wall-s" ) {
        fromTemplate( "wall-w" ) {
            rotate(-90);
        }
        
        Vector3f oMin = new Vector3f( 0, 0.5f, 0 );
        Vector3f oMax = new Vector3f( 1, 1, 1 );          
        Vector3f min = oMin.subtract( 0.5f, 0.5f, 0.5f );
        Vector3f max = oMax.subtract( 0.5f, 0.5f, 0.5f );
        
        up( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad(0) { retexture(Direction.UP) }
        }
        
        down() { // ShapeIndex.getRect(min.x, min.y, max.x, max.y) ) {
            quad(0) { retexture(Direction.DOWN) }
        }
    }


    addTemplate( "pillar-nw" ) {
 
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 0.5f, 0.5f, 1 );          
    
        collider( new CubeCollider( oMin, oMax ) );

        Vector3f min = oMin.subtract( 0.5f, 0.5f, 0.5f );
        Vector3f max = oMax.subtract( 0.5f, 0.5f, 0.5f );
        
        north( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( max.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        internal( Direction.SOUTH ) { //south( ShapeIndex.getRect(min.x, min.z, max.x, max.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        west( ShapeIndex.getRect(oMin.y, oMin.z, oMax.y, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        internal( Direction.EAST ) {
 
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        up( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad {
                material(0);
                
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 0, 1 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
            }
        }

    }

    addTemplate( "pillar-ne" ) {
        fromTemplate( "pillar-nw" ) {
            rotate(90);
        }
        up() { 
            quad(0) { retexture(Direction.UP) }
        }
        down() { 
            quad(0) { retexture(Direction.DOWN) }
        }
    }
    addTemplate( "pillar-sw" ) {
        fromTemplate( "pillar-nw" ) {
            rotate(-90);
        }
        up() { 
            quad(0) { retexture(Direction.UP) }
        }
        down() { 
            quad(0) { retexture(Direction.DOWN) }
        }
    }
    addTemplate( "pillar-se" ) {
        fromTemplate( "pillar-nw" ) {
            rotate(180);
        }
        up() { 
            quad(0) { retexture(Direction.UP) }
        }
        down() { 
            quad(0) { retexture(Direction.DOWN) }
        }
    }

    addTemplate( "beam-dn-w" ) {
 
        Vector3f oMin = new Vector3f( 0, 0, 0 );
        Vector3f oMax = new Vector3f( 0.5f, 1, 0.5f );          
    
        collider( new CubeCollider( oMin, oMax ) );

        Vector3f min = oMin.subtract( 0.5f, 0.5f, 0.5f );
        Vector3f max = oMax.subtract( 0.5f, 0.5f, 0.5f );
        
        north( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( max.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        south( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        west( ShapeIndex.getRect(oMin.y, oMin.z, oMax.y, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        internal( Direction.EAST ) {
 
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        internal( Direction.UP ) { //up( ShapeIndex.getRect(min.x, min.y, max.x, max.y) ) {
            quad {
                material(0);
                
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }
        
        down( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 0, 1 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
            }
        }

    }
    addTemplate( "beam-dn-e" ) {
        fromTemplate( "beam-dn-w" ) {
            rotate(180);
        }
        internal(Direction.UP) { 
            quad(0) { retexture(Direction.UP) }
        }
        down() { 
            quad(0) { retexture(Direction.DOWN) }
        }
    }
    addTemplate( "beam-dn-n" ) {
        fromTemplate( "beam-dn-w" ) {
            rotate(90);
        }
        internal(Direction.UP) { 
            quad(0) { retexture(Direction.UP) }
        }
        down() { 
            quad(0) { retexture(Direction.DOWN) }
        }
    }
    addTemplate( "beam-dn-s" ) {
        fromTemplate( "beam-dn-w" ) {
            rotate(-90);
        }
        internal(Direction.UP) { 
            quad(0) { retexture(Direction.UP) }
        }
        down() { 
            quad(0) { retexture(Direction.DOWN) }
        }
    }

    addTemplate( "beam-up-w" ) {
 
        Vector3f oMin = new Vector3f( 0, 0, 0.5f );
        Vector3f oMax = new Vector3f( 0.5f, 1, 1 );          
   
        collider( new CubeCollider( oMin, oMax ) );

        Vector3f min = oMin.subtract( 0.5f, 0.5f, 0.5f );
        Vector3f max = oMax.subtract( 0.5f, 0.5f, 0.5f );
        
        north( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( max.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        south( ShapeIndex.getRect(oMin.x, oMin.z, oMax.x, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        west( ShapeIndex.getRect(oMin.y, oMin.z, oMax.y, oMax.z) ) {
 
            quad {
                material(0);
                
                vertex( min.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( min.x, max.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        internal( Direction.EAST ) {
 
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }

        up( ShapeIndex.getRect(oMin.x, oMin.y, oMax.x, oMax.y) ) {
            quad {
                material(0);
                
                vertex( min.x, max.y, max.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, max.y, max.z ) {
                    texture( 1, 0 );
                }
                vertex( max.x, min.y, max.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, min.y, max.z ) {
                    texture( 0, 1 );
                }
            }
        }
                
        internal( Direction.DOWN ) { 
            quad {
                material(0);
                
                vertex( max.x, max.y, min.z ) {
                    texture( 1, 1 );
                }
                vertex( min.x, max.y, min.z ) {
                    texture( 0, 1 );
                }
                vertex( min.x, min.y, min.z ) {
                    texture( 0, 0 );
                }
                vertex( max.x, min.y, min.z ) {
                    texture( 1, 0 );
                }
            }
        }

    }
    addTemplate( "beam-up-e" ) {
        fromTemplate( "beam-up-w" ) {
            rotate(180);
        }
        internal(Direction.DOWN) { 
            quad(0) { retexture(Direction.DOWN) }
        }
        up() { 
            quad(0) { retexture(Direction.UP) }
        }
    }
    addTemplate( "beam-up-n" ) {
        fromTemplate( "beam-up-w" ) {
            rotate(90);
        }
        internal(Direction.DOWN) { 
            quad(0) { retexture(Direction.DOWN) }
        }
        up() { 
            quad(0) { retexture(Direction.UP) }
        }
    }
    addTemplate( "beam-up-s" ) {
        fromTemplate( "beam-up-w" ) {
            rotate(-90);
        }
        internal(Direction.DOWN) { 
            quad(0) { retexture(Direction.DOWN) }
        }
        up() { 
            quad(0) { retexture(Direction.UP) }
        }
    }


    addTemplate( "angle-nw" ) {    
        collider( new AngleCollider( Direction.NORTH ) );
        
        volume( 0.5 );
        
        north( ShapeIndex.UNIT_SQUARE ) {
            
            quad {
                material(0);
                
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }

        west( ShapeIndex.UNIT_SQUARE ) {
            
            quad {
                material(0);
                
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, -0.5, 0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
        
        up( ShapeIndex.getTriangle( 0, 0, 1, 1, FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, 0.5, 0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, -0.5, 0.5) {
                    texture(1,1);
                }
                
                vertex(-0.5, -0.5, 0.5) {
                    texture(0,1);
                } 
            }         
        }
 
        down( ShapeIndex.getTriangle( 0, 0, 1, 1, FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(0,1);
                } 
            }         
        }
        
        internal {
            quad {
                material(100);
                
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( 0.5, -0.5, 0.5 ) {
                    texture( 1, 1 );
                }
                vertex( -0.5, 0.5, 0.5 ) {
                    texture( 0, 1 );
                }
                allNormals( 1, 1, 0 ).normalizeLocal();
                allTangents( 1, -1, 0 ).normalizeLocal();
            }
        }
    }

    addTemplate( "angle-ne" ) {
        fromTemplate( "angle-nw" ) {
            rotate(90);
        }
        down() { 
            triangle(0) { retexture(Direction.DOWN) }
        }
        up() { 
            triangle(0) { retexture(Direction.UP) }
        }
    }    
    addTemplate( "angle-se" ) {
        fromTemplate( "angle-nw" ) {
            rotate(180);
        }
        down() { 
            triangle(0) { retexture(Direction.DOWN) }
        }
        up() { 
            triangle(0) { retexture(Direction.UP) }
        }
    }
    addTemplate( "angle-sw" ) {
        fromTemplate( "angle-nw" ) {
            rotate(-90);
        }
        down() { 
            triangle(0) { retexture(Direction.DOWN) }
        }
        up() { 
            triangle(0) { retexture(Direction.UP) }
        }
    }
    
    addTemplate( "slope-corner-ne" ) {
 
        // A wedge is half, so half of that again... may not be right
        // but it's better than nothing.        
        volume( 0.25 );
    
        // For now there is no good corner collider
        // Though we could lower it's profile a bit to
        // make it less obvious.
        collider( new CubeCollider("slope-corner") );
    
        // The ne sloping corner is solid to the bottom, south, and west
        // Down is slightly unconventional because it splits the quad so
        // that the texture can be rotated.
 
        down( ShapeIndex.UNIT_SQUARE ) {
        
            // Connects to the base of solid-south slopes
            triangle {
                material(0);
                
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 1, 0 );
                }
                vertex( -0.5, -0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 0, 1 );
                }
            }
                            
            // Connects to the base of solid-west slopes
            triangle {
                material(0);
                
                vertex( -0.5, 0.5, -0.5 ) {
                    texture( 0, 0 );
                }
                vertex( 0.5, -0.5, -0.5 ) {
                    texture( 1, 1 );
                }
                vertex( 0.5, 0.5, -0.5 ) {
                    texture( 0, 1 );
                }
            }
        }
 
        west( ShapeIndex.getTriangle( 0, 0, 1, 1, -FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(0,1);
                }
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, 0.5, 0.5) {
                    texture(1,1);
                } 
            }         
        }

        south( ShapeIndex.getTriangle( 0, 0, 1, 1, FastMath.sqrt(2), FastMath.sqrt(2) ) ) {
            triangle {
                material(0);
                
                vertex(-0.5, 0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, 0.5, -0.5) {
                    texture(1,1);
                }
                
                vertex(-0.5, 0.5, 0.5) {
                    texture(0,1);
                } 
            }         
        }
 
        internal {
 
            triangle {
                material(100);
                
                vertex(0.5, -0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(-0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, 0.5, 0.5 ) {
                    texture(1,1);
                }
                
                allNormals(0, -1, 1).normalizeLocal();
                allTangents(-1, 0, 0 ); 
            }
             
            triangle {
                material(100);
                
                vertex(0.5, 0.5, -0.5) {
                    texture(0,0);
                }
                
                vertex(0.5, -0.5, -0.5) {
                    texture(1,0);
                }
                
                vertex(-0.5, 0.5, 0.5 ) {
                    texture(0,1);
                }
                
                allNormals(1, 0, 1).normalizeLocal();
                allTangents(0, -1, 0 ); 
            }
        }                 
    }
    
    addTemplate( "slope-corner-nw" ) {
        fromTemplate( "slope-corner-ne" ) {
            rotate(-90);
        }
    }    
    addTemplate( "slope-corner-sw" ) {
        fromTemplate( "slope-corner-ne" ) {
            rotate(180);
        }
    }
    addTemplate( "slope-corner-se" ) {
        fromTemplate( "slope-corner-ne" ) {
            rotate(90);
        }
    }
    
}
