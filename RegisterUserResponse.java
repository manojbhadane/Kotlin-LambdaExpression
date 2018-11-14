package com.springtech.responsemodel;

/**
 * Created by nilesh.patil on 05-02-2018.
 */

public class RegisterUserResponse extends BaseResponseModel
{
    private Result Result;

    public Result getResult()
    {
        return Result;
    }

    public void setResult(Result result)
    {
        Result = result;
    }


    public class Result{
        private String UserId;

        public String getUserId()
        {
            return UserId;
        }

        public void setUserId(String userId)
        {
            UserId = userId;
        }
    }
}
