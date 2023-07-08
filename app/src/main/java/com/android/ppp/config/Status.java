package com.android.ppp.config;

public enum Status
{
    Success,
    UnknownError,
    ArgumentNullException,
    ArgumentOutOfRangeException,
    DatabaseAccessException,
    MacIsNullOrEmpty,
    MacAddressNotExists,
    PasswordIsNullOrEmpty,
    PasswordAreIllegal,
    PasswordNotEquals,
    EmailIsNullOrEmpty,
    EmailAddressIsExists,
    EmailAddressNotExists,
    EmailAddressAreIllegal,
    ConfigurationError,
    UnableToCreateOrder,
    OrderNotExists,
    OrderStatusError,
    NotAllowBuyService,
    GithubAccessException,
    ActivationCodeNotExists,
    IsUseActivationCode,
    NotAllowUseActivationCode,
    MultipleOnline,
    SerializationProtocolError
}