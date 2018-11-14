package com.springtech.responsemodel;

/**
 * Created by nilesh.patil on 05-02-2018.
 */

public class BaseResponseModel
{
    private boolean IsError;
    private String Message;
    private int ErrorCode;
    public boolean isError()
    {
        return IsError;
    }

    public void setError(boolean error)
    {
        IsError = error;
    }

    public String getMessage()
    {
        return Message;
    }

    public void setMessage(String message)
    {
        Message = message;
    }

    public int getErrorCode()
    {
        return ErrorCode;
    }

    public void setErrorCode(int errorCode)
    {
        ErrorCode = errorCode;
    }

}
