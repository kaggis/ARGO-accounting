package org.accounting.system.services.client;

import io.quarkus.mongodb.panache.PanacheQuery;
import io.quarkus.oidc.TokenIntrospection;
import io.quarkus.oidc.UserInfo;
import org.accounting.system.dtos.client.ClientResponseDto;
import org.accounting.system.dtos.pagination.PageResource;
import org.accounting.system.entities.client.Client;
import org.accounting.system.mappers.ClientMapper;
import org.accounting.system.repositories.client.ClientRepository;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.core.UriInfo;
import java.util.Objects;

@ApplicationScoped
public class ClientService {

    @Inject
    ClientRepository clientRepository;

    @Inject
    TokenIntrospection tokenIntrospection;

    @Inject
    UserInfo userInfo;

    @ConfigProperty(name = "key.to.retrieve.id.from.access.token")
    String key;



    /**
     * This method extracts specific information regarding the client from access token and stores it into the
     * Accounting System database.
     *
     * @return The registered client has been turned into a response body.
     */
    public ClientResponseDto register(){

        String id = tokenIntrospection.getJsonObject().getString(key);

        if(StringUtils.isEmpty(id)){
            throw new ForbiddenException("voperson_id is empty. The client cannot be registered without voperson_id.");
        }

        String name = Objects.isNull(userInfo.getJsonObject().get("name")) ? "": userInfo.getJsonObject().getString("name");
        String email = Objects.isNull(userInfo.getJsonObject().get("email")) ? "": userInfo.getJsonObject().getString("email");

        Client client = new Client();

        client.setId(id);
        client.setName(name);
        client.setEmail(email);
        client.setCreatorId(id);

        clientRepository.persistOrUpdate(client);

       return ClientMapper.INSTANCE.clientToResponse(client);
    }

    /**
     * Returns the N Clients from the given page.
     *
     * @param page Indicates the page number.
     * @param size The number of Clients to be retrieved.
     * @param uriInfo The current uri.
     * @return An object represents the paginated results.
     */
    public PageResource<Client, ClientResponseDto> findAllClientsPageable(int page, int size, UriInfo uriInfo){

        PanacheQuery<Client> panacheQuery = clientRepository.findAllPageable(page, size);

        return new PageResource<>(panacheQuery, ClientMapper.INSTANCE.clientsToResponse(panacheQuery.list()), uriInfo);
    }
}
