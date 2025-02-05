package in.fortytwo42.adapter.cam.util;

import org.keycloak.representations.idm.ClientRepresentation;

import in.fortytwo42.adapter.cam.dto.ClientTO;

public class MappingUtil {

    public static ClientRepresentation getClientRepresentation(ClientTO clientCreationDto) {
        ClientRepresentation clientRepresentation = new ClientRepresentation();
        clientRepresentation.setClientId(clientCreationDto.getClientId());
        clientRepresentation.setSecret(clientCreationDto.getSecret());
        clientRepresentation.setDescription(clientCreationDto.getClientDescription());
        clientRepresentation.setEnabled(clientCreationDto.getIsEnabled());
        clientRepresentation.setName(clientCreationDto.getClientName());
        clientRepresentation.setDirectAccessGrantsEnabled(clientCreationDto.getDirectAccessGrantEnabled());
        clientRepresentation.setServiceAccountsEnabled(clientCreationDto.getServiceAccountEnabled());
        clientRepresentation.setPublicClient(clientCreationDto.getPublicClient());
        clientRepresentation.setAuthorizationServicesEnabled(clientCreationDto.getAuthorizationEnabled());
        clientRepresentation.setAdminUrl(clientCreationDto.getAdminUrl());
        clientRepresentation.setRedirectUris(clientCreationDto.getRedirectUris());
        clientRepresentation.setBearerOnly(clientCreationDto.getBearerOnly());
        clientRepresentation.setAttributes(clientCreationDto.getAttributes());
        clientRepresentation.setBaseUrl(clientCreationDto.getBaseUrl());
        clientRepresentation.setFullScopeAllowed(clientCreationDto.getFullScopeAllowed());
        clientRepresentation.setWebOrigins(clientCreationDto.getWebOrigins());
        clientRepresentation.setStandardFlowEnabled(clientCreationDto.getStandardFlowEnabled());
        clientRepresentation.setRootUrl(clientCreationDto.getRootUrl());
        clientRepresentation.setDirectAccessGrantsEnabled(Boolean.TRUE);
        clientRepresentation.setImplicitFlowEnabled(clientCreationDto.getImplicitFlowEnabled());
        return clientRepresentation;

    }
}
