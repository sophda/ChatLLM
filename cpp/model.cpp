#include <iostream>
#include <memory>
#include "llm/llm.hpp"
#include <vector>
#include "model.hpp"
using namespace MNN::Transformer;

Model::Model() {
    this->executor =  MNN::Express::Executor::newExecutor(MNN_FORWARD_CPU, backendConfig, 1);
    this->pscope = std::make_shared<MNN::Express::ExecutorScope>(executor);
    
    // Constructor impl ementation (if needed)
}

Model::~Model() {
    // Destructor implementation (if needed)
}

void Model::init(const std::string& filepath) {
    // Load model from file
    // this->scope = this->executor;
    
    
    this->llm = std::unique_ptr<Llm>(Llm::createLLM(filepath));
    this->llm->set_config("{\"tmp_path\":\"tmp\"}");
    bool res = this->llm->load();
    if (!res) {
        MNN_ERROR("LLM init error\n");
        return ;
    }
    this->llm->tuning(OP_ENCODER_NUMBER, {1, 5, 10, 20, 30, 50, 100});
    this->messages.emplace_back("system", "You are a helpful assistant.");
    this->context = this->llm->getContext();
}

void Model::predict(std::string &input, std::string &output) {
    // Make a prediction based on the input
    this->messages.emplace_back("user", input);
    this->llm->response(this->messages);
    auto assistant_str = this->context->generate_str;
    output = assistant_str;
    this->messages.emplace_back("assistant", assistant_str);

}
