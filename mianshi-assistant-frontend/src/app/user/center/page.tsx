"use client";
import "./index.css";
import {Avatar, Card, Col, Row} from "antd";
import {useSelector} from "react-redux";
import {RootState} from "@/stores";
import Title from "antd/es/typography/Title";
import Paragraph from "antd/es/typography/Paragraph";
import {useState} from "react";
import CalendarChart from "@/app/user/center/components/CalendarChart/page";

/**
 * 用户中心页面
 * @constructor
 */
export default function UserCenterPage() {

    // 获取登录用户信息
    const loginUser = useSelector((state: RootState) => state.loginUser);
    // 便于复用
    const user = loginUser;
    // 控制菜单栏的 Tab 高亮
    const [activeTabKey, setActiveTabKey] = useState<string>("record");


    return (
        <div id="userCenterPage">
            <Row gutter={[16, 16]}>
                <Col xs={24} md={6}>
                    <Card style={{textAlign: "center"}}>
                        <Avatar src={user.userAvatar} size={72}/>
                        <div style={{marginBottom: 16}}></div>
                        <Card.Meta
                            title={<Title level={4}>
                                {user.userName}
                            </Title>}
                            description={<Paragraph type={"secondary"}>
                                {user.userProfile}
                            </Paragraph>}
                        />
                    </Card>
                </Col>
                <Col xs={24} md={18}>
                    <Card tabList={[
                        {
                            key: "record",
                            label: "刷题记录",
                        },
                        {
                            key: "others",
                            label: "其他",
                        },
                    ]}
                          activeTabKey={activeTabKey}
                          onTabChange={(key) => setActiveTabKey(key)}
                    >
                        {activeTabKey === 'record' && <CalendarChart/>}
                        {activeTabKey === 'others' && <div>bbb</div>}
                    </Card>
                </Col>
            </Row>
        </div>
    )
}
