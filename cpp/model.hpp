#include <cstddef>
#include <iostream>
#include <fstream>
#include <memory>
#include <vector>
#include <string>
#include "MNN/expr/Executor.hpp"
#include "llm/llm.hpp"

#include <MNN/AutoTime.hpp>
#include <MNN/expr/ExecutorScope.hpp>
#include <fstream>
#include <sstream>
#include <stdlib.h>
#include <initializer_list>
using namespace MNN::Transformer;
class Model {
public:
    Model();
    ~Model();

    void init(const std::string& filepath);
    void predict(std::string &input, std::string &output);
    // void save(const std::string& filepath) const;

    // void train(const std::vector<std::vector<double>>& data, const std::vector<int>& labels);
    // int predict(const std::vector<double>& sample) const;

private:
    MNN::BackendConfig backendConfig;
    std::shared_ptr<MNN::Express::Executor> executor;
    std::shared_ptr<MNN::Express::ExecutorScope> pscope;
    std::unique_ptr<Llm> llm;
    // std::string config_path;
    ChatMessages messages;
    const LlmContext* context = nullptr;
 };