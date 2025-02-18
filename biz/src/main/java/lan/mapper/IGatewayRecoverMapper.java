
package lan.mapper;

import com.tuya.luban.biz.service.lan.domains.recover.GatewayRecoverDTO;
import com.tuya.luban.core.dao.domains.recover.GatewayRecoverDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @author : patrickkk
 * @date 2021-11-12
 */
@Mapper
public interface IGatewayRecoverMapper {

    IGatewayRecoverMapper INSTANCE = Mappers.getMapper(IGatewayRecoverMapper.class);

    List<GatewayRecoverDTO> toGatewayRecoverDTOList(List<GatewayRecoverDO> gatewayRecovers);
}
