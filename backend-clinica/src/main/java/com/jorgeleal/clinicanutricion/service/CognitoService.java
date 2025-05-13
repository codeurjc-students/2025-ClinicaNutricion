package com.jorgeleal.clinicanutricion.service;
import com.jorgeleal.clinicanutricion.dto.UserDTO;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminAddUserToGroupRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminCreateUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminDeleteUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminUpdateUserAttributesRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;
import org.springframework.stereotype.Service;

@Service
public class CognitoService {
    private String userPoolId = System.getenv("COGNITO_USER_POOL_ID") != null 
    ? System.getenv("COGNITO_USER_POOL_ID") 
    : "eu-west-3_akIyCC7tP";

    private final CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
        .region(Region.EU_WEST_3)
        .build();


    public String createCognitoUser(UserDTO userDTO) {
        String groupName = userDTO.getUserType();
        String temporaryPassword = "Contraseña123!";
    
        try {
            AdminCreateUserRequest createUserRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userDTO.getMail())
                    .temporaryPassword(temporaryPassword)
                    .messageAction("SUPPRESS")
                    .userAttributes(
                            AttributeType.builder().name("email").value(userDTO.getMail()).build(),
                            AttributeType.builder().name("given_name").value(userDTO.getName()).build(),
                            AttributeType.builder().name("family_name").value(userDTO.getSurname()).build(),
                            AttributeType.builder().name("birthdate").value(userDTO.getBirthDate().toString()).build(),
                            AttributeType.builder().name("gender").value(userDTO.getGender()).build(),
                            AttributeType.builder().name("phone_number").value(userDTO.getPhone()).build(),
                            AttributeType.builder().name("email_verified").value("true").build(),
                            AttributeType.builder().name("phone_number_verified").value("true").build()
                    )
                    .build();
    
            var createdUser = cognitoClient.adminCreateUser(createUserRequest);
            String cognitoUserId = createdUser.user().username();
    
            AdminAddUserToGroupRequest addUserToGroupRequest = AdminAddUserToGroupRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userDTO.getMail())
                    .groupName(groupName)
                    .build();
    
            cognitoClient.adminAddUserToGroup(addUserToGroupRequest);
            return cognitoUserId;
        } catch (UsernameExistsException e) {
            throw new RuntimeException("El usuario con el correo " + userDTO.getMail() + " ya existe.");
        }
    }
        

    public void updateCognitoUser(UserDTO userDTO) {
        AdminUpdateUserAttributesRequest updateRequest = AdminUpdateUserAttributesRequest.builder()
            .userPoolId(userPoolId)
            .username(userDTO.getMail())
            .userAttributes(
                AttributeType.builder().name("email").value(userDTO.getMail()).build(),
                AttributeType.builder().name("given_name").value(userDTO.getName()).build(),
                AttributeType.builder().name("family_name").value(userDTO.getSurname()).build(),
                AttributeType.builder().name("birthdate").value(userDTO.getBirthDate().toString()).build(),
                AttributeType.builder().name("gender").value(userDTO.getGender()).build(),
                AttributeType.builder().name("phone_number").value(userDTO.getPhone()).build()
            )
            .build();

        cognitoClient.adminUpdateUserAttributes(updateRequest);
    }

    public void deleteCognitoUser(String username) {
        AdminDeleteUserRequest deleteRequest = AdminDeleteUserRequest.builder()
            .userPoolId(userPoolId)
            .username(username)
            .build();
        cognitoClient.adminDeleteUser(deleteRequest);
    }

    public void addUserToPatientGroup(String sub) {
        final String groupName = "patient";
        AdminAddUserToGroupRequest request = AdminAddUserToGroupRequest.builder()
            .userPoolId(userPoolId)
            .username(sub)
            .groupName(groupName)
            .build();

        cognitoClient.adminAddUserToGroup(request);
    }
}