import {Button, Form, message, Modal, Select} from "antd";
import React, {useEffect, useState} from "react";
import {batchRemoveQuestionsFromBankUsingPost,} from "@/api/questionBankQuestionController";
import {listQuestionBankVoByPageUsingPost} from "@/api/questionBankController";
import QuestionBankVO = API.QuestionBankVO;

interface Props {
    questionIdList?: number[];
    visible: boolean;
    onSubmit: () => void;
    onCancel: () => void;
}

/**
 * 更新题目所属题库弹窗
 * @param props
 * @constructor
 */
const BatchRemoveQuestionsFromBankModal: React.FC<Props> = (props) => {
    const {questionIdList, visible, onSubmit, onCancel} = props;
    const [form] = Form.useForm();
    const [questionBankList, setQuestionBankList] = useState<
        API.QuestionBankVO[]
    >([]);

    // 获取题库列表
    const getQuestionBankList = async () => {
        // 题库数量不多，直接全量获取
        const pageSize = 200;

        try {
            const res = await listQuestionBankVoByPageUsingPost({
                pageSize,
                sortField: "createTime",
                sortOrder: "descend",
            });
            setQuestionBankList(res.data?.records as QuestionBankVO[] ?? []);
        } catch (e: any) {
            message.error("获取题库列表失败，" + e.message);
        }
    };

    // 保证只执行一次
    useEffect(() => {
        getQuestionBankList();
    }, []);

    /**
     * 提交
     *
     * @param fields
     */
    const doSubmit = async (fields: API.QuestionBankQuestionBatchRemoveRequest) => {
        const hide = message.loading("正在操作...");
        const questionBankId = fields.questionBankId;
        try {
            await batchRemoveQuestionsFromBankUsingPost({
                questionIdList, questionBankId
            });
            hide();
            message.success("操作成功");
            onSubmit?.();
        } catch (e: any) {
            hide();
            message.error("操作失败，" + e.message);
        }
    }

    return (
        <Modal
            destroyOnClose
            title={"批量从题库移除题目"}
            open={visible}
            footer={null}
            onCancel={() => {
                onCancel?.();
            }}
        >
            <Form form={form} style={{marginTop: 24}} onFinish={doSubmit}>
                <Form.Item label="选择题库" name="questionBankId">
                    <Select
                        style={{width: "100%"}}
                        options={questionBankList.map((questionBank) => {
                            return {
                                label: questionBank.title,
                                value: questionBank.id,
                            };
                        })}
                    />
                </Form.Item>
                <Form.Item>
                    <Button type={"primary"} htmlType={"submit"}>提交</Button>
                </Form.Item>
            </Form>
        </Modal>
    );
};
export default BatchRemoveQuestionsFromBankModal;