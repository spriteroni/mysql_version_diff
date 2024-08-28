import * as React from 'react';
import {Modal, Typography, Button} from 'antd';
const {Title, Paragraph} = Typography;

const Declaration = (props) => {
  const {visible, onClose} = props;

  const confirm = () => {
    onClose && onClose();
  };

  return (
    <Modal title={<Title level={5}>免责声明</Title>} width={'60%'} open={visible} footer={null} onCancel={confirm}>
      <Typography>
        <Paragraph>在使用跨版本兼容性检测工具前，请仔细阅读以下免责声明：</Paragraph>
        <Paragraph strong>
          跨版本兼容性检测工具（以下简称“工具”）旨在帮助用户检测MySQL数据库版本间的兼容性问题。工具的开发者（以下简称“开发者”）不对因使用本工具而造成的任何直接或间接的损失承担责任。
        </Paragraph>
        <Paragraph strong>
          1. 使用限制：用户应当在合法和正当的范围内使用本工具，不得利用本工具进行非法活动或侵犯他人权益的行为。
        </Paragraph>
        <Paragraph strong>
          2.
          信息准确性：工具提供的结果基于开发者设定的检测逻辑，不保证完全准确无误。用户在使用结果做决策时，应自行承担风险。
        </Paragraph>
        <Paragraph strong>
          3. 功能限制：工具可能无法覆盖所有兼容性问题。用户应自行验证迁移过程中的其他潜在问题。
        </Paragraph>
        <Paragraph strong>
          4.
          技术支持：工具是按“现状”提供的，开发者不提供任何形式的明示或暗示担保，包括但不限于适销性、特定用途适用性的担保。
        </Paragraph>
        <Paragraph strong>
          5.
          更新和维护：开发者可能会不定期更新工具，以修复已知问题或增加新功能。用户应当自行关注更新信息并选择是否安装更新。
        </Paragraph>
        <Paragraph strong>
          6.
          反馈：如用户在使用过程中发现问题或有改进建议，欢迎向开发者反馈。但开发者对于是否采纳反馈内容享有最终决定权。
        </Paragraph>
        <Paragraph strong>7. 法律适用：本声明的解释、效力及争议解决均适用开发者所在地的法律。</Paragraph>
        <Paragraph strong>
          8.
          账户安全：本工具对用户的账户信息不作远程存储处理，只存储在本地浏览器LocalStorage中，账户信息请妥善保管，本工具不承担任何账户遗失的风险。
        </Paragraph>
        <Paragraph strong>
          继续使用本工具即表示您已阅读并同意上述条款。如果您不同意这些条款，请立即停止使用本工具。
        </Paragraph>
      </Typography>
      <div style={{width: '100%', height: '32px'}}></div>
      <Button style={{position: 'absolute', right: '24px', bottom: '24px'}} type="primary" onClick={confirm}>
        知道了
      </Button>
    </Modal>
  );
};

export default Declaration;
