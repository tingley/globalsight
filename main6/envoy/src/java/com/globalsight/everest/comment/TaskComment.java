package com.globalsight.everest.comment;

import java.util.Date;

import com.globalsight.everest.foundation.WorkObject;

/**
 * <p>
 * For GBS-3780 jobs have comments from different companies. Splits the comments
 * table into 2 tables (job comments, activity comments).
 * <p>
 * Just for hibernate mapping, please don't use it in your code.
 */
public class TaskComment extends CommentImpl
{
    private static final long serialVersionUID = 382373090691513839L;
    
    /**
     * Default constructor.
     */
    public TaskComment()
    {
    }

    /**
     * Constructs a TaskComment.
     */
    public TaskComment(Date p_createDate, String p_creatorId, String p_comment,
            WorkObject p_object)
    {
        super(p_createDate, p_creatorId, p_comment, p_object);
        
        //For GBS-3780. 
        // There are a lot of code is use ID to get the corresponding
        // files, so it is needed that keep the id is unique in job comments and
        // activity comment.
        this.setId(CommentUtil.createUuid());
    }
}
