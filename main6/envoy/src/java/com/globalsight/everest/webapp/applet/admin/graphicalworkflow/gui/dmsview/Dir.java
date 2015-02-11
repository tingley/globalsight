/**
 *  Copyright 2009 Welocalize, Inc. 
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  
 *  You may obtain a copy of the License at 
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 */
package com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.dmsview;

import java.util.Vector;
import java.util.Enumeration;


import com.globalsight.everest.webapp.applet.admin.graphicalworkflow.gui.util.WindowUtil;
import javax.swing.tree.DefaultMutableTreeNode;
/*import com.ms.security.PolicyEngine;          
import com.ms.security.PermissionID;          
import netscape.security.PrivilegeManager;    */ //no need with Plug In 1.3


class Dir
{
    private byte spaces[] = "                            ".getBytes();
    //private DMS _dms;
    private Vector _vec = new Vector();
    private DefaultMutableTreeNode _root;
    // private DocRepository docRepos;  //Parag commeneted out for now until implementation of workflow object is known

    /**
     * Constructor.
     * @param doc_repos the DocRepository object
     * @roseuid 372F6DCB005C
     */

    public Dir()
    {     //Parag default constructor since we are not using the DocRepository class for now
    }

    /*public Dir(DocRepository doc_repos) {
    
        docRepos = doc_repos;
        _dms = doc_repos.getDMS();
    } */ //Commented out by parag

    /**
     * Loads one level below the root directory of the DMS object
       @roseuid 372F6DCB005E
     */
    //public void loadDirectory() throws ModelInternalException,DMSException {


    //comments today june 8 2002
    public void loadDirectory() throws Exception {
        /*Folder	f = getRootFolder();
        _root = new DefaultMutableTreeNode(f, true);
        loadDirectory( f.getContents(), 1, _root );*/

        // only  for testing 
        Vector displayData = new Vector();
        _root = new DefaultMutableTreeNode(displayData, true);

        displayData.addElement("wwwroot");
        displayData.addElement("ftproot");
        //displayData.add("ftproot");

        loadDirectory( displayData.elements(), 1, _root );

    }   
    /**
     * Returns the root folder of the DMS object.
     * @return the root folder of the DMS object.
     * @roseuid 372F6DCB005F
     */
    //private Folder getRootFolder() throws DMSException,ModelInternalException {
    /*/private Folder getRootFolder() throws Exception {

        //Folder folder = (Folder) docRepos.getRootFolder(_dms);;
        //Parag Commenetd out by parag since we dont use that object yet

        return folder;
    } */
    /**
     * Returns the root node of the DMS object's directory tree hierarchy.
     * @return the root node of the DMS object's directory tree hierarchy.
     * @roseuid 372F6DCB0060
     */
    public DefaultMutableTreeNode getNode()
    {
        return _root;
    }

    // QuickSort: to speed up the expanding of directory

    //Dummy function added by parag,will have to be replaced by the actual function below
    void QuickSort() throws Exception{
    }

    /*void QuickSort(DMSObject v[], int left, int right) throws Exception
    {
      int lo = left;
      int hi = right;
      DMSObject mid;

      if ( right > left)
      {
         mid = v[ ( left + right ) / 2 ];    */    //Parag

    // loop through the array until indices cross
    //while( lo <= hi ) //Parag
    //{  //Parag
    /* find the first element that is greater than or equal to 
     * the partition element starting from the left index.
     */
    //while( ( lo < right ) && ( ( v[lo].toString().toLowerCase()).compareTo(mid.toString().toLowerCase()) < 0 ) )
    //      ++lo;

    /* find an element that is smaller than or equal to 
     * the partition element starting from the right index.
     */
    /* while( ( hi > left ) && ( (v[hi].toString().toLowerCase()).compareTo(mid.toString().toLowerCase()) > 0 ) )
         --hi;

    // if the indexes have not crossed, swap
    if( lo <= hi ) 
    {
       swap(v, lo, hi);
       ++lo;
       --hi;
    }
 }  */

    /* If the right index has not reached the left side of array
     * must now sort the left partition.
     */
    /*if( left < hi )
       QuickSort( v, left, hi );     */

    /* If the left index has not reached the right side of array
     * must now sort the right partition.
     */
    /*if( lo < right )
       QuickSort( v, lo, right );
 }
}  */

    /*private void swap(DMSObject v[], int i, int j)
    {
       DMSObject T;
       T = v[i]; 
       v[i] = v[j];
       v[j] = T;
    } */

    private void swap()      //Dummy function added by Parag

    {
    }


    /* public void sort(DMSObject v[]) throws Exception
     {
        QuickSort(v, 0, v.length - 1);
     }    */                    

    public void sort() throws Exception            //Dummy function added by Parag
    {
        QuickSort();
    }           

    /**
       @roseuid 372F6DCB0066
     */
    /*private boolean sweep(DMSObject[] values) {
        boolean swapped = false;

        // quick sort
        try
        {
            sort(values);
        }
        catch (Exception e)
        {
            System.err.println(" Quick sort exception: " + e);
        }               
        
        return swapped;	
    }   */
    private boolean sweep()
    { //Dummy function added by Parag
        return false;
    }

    /**
       @roseuid 372F6DCB0068
     */
    /*private void sortList(DMSObject[] values) {
        while (sweep(values));
    } */ //Parag

    private void sortList()
    {   //Dummy function added by Parag

    }


    /**
       @roseuid 372F6DCB006A
     */

    // use this method    for loading     DefaultMutableTreeNode
    // delete another same method  .Use only one method
    void loadDirectory(Enumeration displayData , int level, DefaultMutableTreeNode root) throws Exception {   
        if (level == 1)
        {

            //remove the empty node
            if ( displayData.hasMoreElements() )
            {
                root.removeAllChildren();
            }

            //sort all child folders/documents
            //sortList(list);

            // for ( int i = 0 ; i < count ; i++ )
            while (displayData.hasMoreElements())
            {
                DefaultMutableTreeNode node;
                node = new DefaultMutableTreeNode(displayData.nextElement(), true);
                DefaultMutableTreeNode empty_node = new DefaultMutableTreeNode();
                node.add(empty_node);
                root.add(node);
            }
        }
        else if (level == 2)
        {
            /*  while(displayData.hasMoreElements())
             {            {
                 DefaultMutableTreeNode _node;
 
                 //to find the node corresponding to the folder
                 for (int j = 0; j < root.getChildCount(); j++)
                 {
                     _node = (DefaultMutableTreeNode)root.getChildAt(j);
                     if ( !_node.isLeaf() && (Vector) _node.getUserObject() == folder )
                     {
                         loadDirectory( displayData.el, 1, _node );
                     }
                 }
 
             }*/
        }



    }

    void loadDirectory(Vector displayData , int level, DefaultMutableTreeNode root) throws Exception {   
        if (level == 1)
        {
            int count = displayData.size();

            //remove the empty node
            if ( count != 0 )
            {
                root.removeAllChildren();
            }

            //sort all child folders/documents
            //sortList(list);

            for ( int i = 0 ; i < count ; i++ )
            {
                DefaultMutableTreeNode node;
                node = new DefaultMutableTreeNode(displayData.elementAt(i), true);
                DefaultMutableTreeNode empty_node = new DefaultMutableTreeNode();
                node.add(empty_node);
                root.add(node);
            }
        }
        else if (level == 2)
        {
            int count = displayData.size();


            for ( int i = 0 ; i < count ; i++ )
            {
                DefaultMutableTreeNode _node;

                //to find the node corresponding to the folder
                for (int j = 0; j < root.getChildCount(); j++)
                {
                    _node = (DefaultMutableTreeNode)root.getChildAt(j);
                    /*if ( !_node.isLeaf() && (Folder) _node.getUserObject() == folder )
                    {
                        loadDirectory( folder.getContents(), 1, _node );
                    }*/
                }

            }
        }



    }



    //Dummy function added by Parag
    // void loadDirectory(DMSObject[] list, int level, DefaultMutableTreeNode root) throws DMSException,ModelInternalException {   
    /*    if(WindowUtil.isUsingNetscape())
        {
            try
            {				
                PrivilegeManager.enablePrivilege("UniversalFileAccess");
            }
            catch (Exception ex) 
            {
                Log.println(Log.LEVEL2, "AdminApplet.init - Could not enable netscape.security Privileges" + ex );
            }
        } */

    /* if (level == 3)
     return;
     
 if (level == 1)
 {
     int count = list.length;
     
     //remove the empty node
     if ( count != 0 )
     {
         root.removeAllChildren();
     }
     
     //sort all child folders/documents
     sortList(list);
     
     for( int i = 0 ; i < count ; i++ ) {
         DefaultMutableTreeNode node;
         if( list[ i ].getType() == DMSObject.TYPE_FOLDER ) 
         {
             node = new DefaultMutableTreeNode(list[i], true);
             DefaultMutableTreeNode empty_node = new DefaultMutableTreeNode();
             node.add(empty_node);
             
             Folder	folder = (Folder) list[ i ];
         } 
         else
         {
             node = new DefaultMutableTreeNode(list[i], false);
         }
         root.add(node);
     }
 }
 else if (level == 2)
 {
     int count = list.length;
 
     for( int i = 0 ; i < count ; i++ ) 
     {
         if( list[ i ].getType() == DMSObject.TYPE_FOLDER ) 
         {    		   
             Folder	folder = (Folder) list[ i ];
             DefaultMutableTreeNode _node;
             
             //to find the node corresponding to the folder
             for (int j = 0; j < root.getChildCount(); j++)
             {
                 _node = (DefaultMutableTreeNode)root.getChildAt(j);
                 if ( !_node.isLeaf() && (Folder) _node.getUserObject() == folder )
                 {   
                     loadDirectory( folder.getContents(), 1, _node );
                 }
             }
         } 
     }
 }
}
}          */ //Parag
}

