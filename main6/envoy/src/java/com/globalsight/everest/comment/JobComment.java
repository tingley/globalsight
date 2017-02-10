package com.globalsight.everest.comment;

import java.util.Date;

import com.globalsight.everest.foundation.WorkObject;

/**
 * <p>
 * For GBS-3780 jobs have comments from different companies. Splits the comments
 * table into 3 tables (job comments, workflow comments, activity comments).
 * <p>
 * Just for hibernate mapping, please don't use it in your code.
 */
public class JobComment extends CommentImpl
{
    private static final long serialVersionUID = 7812098653326408875L;
    
    /**
     * Default constructor.
     */
    public JobComment()
    {
    }

    /**
     * Constructs a JobComment.
     */
    public JobComment(Date p_createDate, String p_creatorId, String p_comment,
            WorkObject p_object)
    {
        super(p_createDate, p_creatorId, p_comment, p_object);
        
        //For GBS-3780. 
        // There are a lot of code use ID to get the corresponding
        // files, so it is needed that keep the id is unique in job comments and
        // activity comment.
        this.setId(CommentUtil.createUuid());
    }
}
