#!/bin/bash

# SkyTrust无人机共享租赁平台 - 部署脚本
# 版本: 1.0.0

set -e  # 遇到错误时退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 显示帮助信息
show_help() {
    echo "SkyTrust平台部署脚本"
    echo ""
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --help             显示帮助信息"
    echo "  -e, --env ENV          部署环境 (dev/test/prod), 默认: dev"
    echo "  -c, --clean            清理构建缓存"
    echo "  -b, --build            构建应用"
    echo "  -d, --docker           使用Docker部署"
    echo "  -k, --k8s              使用Kubernetes部署"
    echo "  -t, --test             运行测试"
    echo "  --skip-tests           跳过测试"
    echo "  --db-migrate           运行数据库迁移"
    echo ""
    echo "示例:"
    echo "  $0 -e dev -b -d        # 开发环境构建并Docker部署"
    echo "  $0 -e prod --k8s       # 生产环境Kubernetes部署"
}

# 环境检查
check_environment() {
    log_info "检查环境依赖..."

    # 检查Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d '"' -f2)
        log_info "Java版本: $JAVA_VERSION"
    else
        log_error "未找到Java，请安装JDK 17+"
        exit 1
    fi

    # 检查Maven
    if command -v mvn &> /dev/null; then
        MAVEN_VERSION=$(mvn -v | grep "Apache Maven" | cut -d ' ' -f3)
        log_info "Maven版本: $MAVEN_VERSION"
    else
        log_error "未找到Maven，请安装Maven 3.8+"
        exit 1
    fi

    # 检查Docker（如果使用Docker部署）
    if [[ $USE_DOCKER == true ]] || [[ $USE_K8S == true ]]; then
        if command -v docker &> /dev/null; then
            DOCKER_VERSION=$(docker --version | cut -d ' ' -f3 | sed 's/,//')
            log_info "Docker版本: $DOCKER_VERSION"
        else
            log_error "未找到Docker，请安装Docker"
            exit 1
        fi
    fi

    # 检查Docker Compose（如果使用Docker部署）
    if [[ $USE_DOCKER == true ]]; then
        if command -v docker-compose &> /dev/null; then
            DOCKER_COMPOSE_VERSION=$(docker-compose --version | cut -d ' ' -f3 | sed 's/,//')
            log_info "Docker Compose版本: $DOCKER_COMPOSE_VERSION"
        else
            log_error "未找到Docker Compose，请安装Docker Compose"
            exit 1
        fi
    fi

    # 检查kubectl（如果使用K8S部署）
    if [[ $USE_K8S == true ]]; then
        if command -v kubectl &> /dev/null; then
            KUBECTL_VERSION=$(kubectl version --client --short | cut -d ' ' -f3)
            log_info "kubectl版本: $KUBECTL_VERSION"
        else
            log_error "未找到kubectl，请安装kubectl"
            exit 1
        fi
    fi
}

# 清理构建缓存
clean_build() {
    log_info "清理构建缓存..."
    mvn clean
    log_success "构建缓存清理完成"
}

# 运行测试
run_tests() {
    log_info "运行测试..."

    if [[ $SKIP_TESTS == true ]]; then
        log_warning "跳过测试"
        return 0
    fi

    # 运行单元测试
    log_info "运行单元测试..."
    mvn test

    # 运行集成测试（如果存在）
    if [[ -f "src/test/java/com/skytrust/integration" ]]; then
        log_info "运行集成测试..."
        mvn verify -Pintegration-test
    fi

    log_success "测试完成"
}

# 构建应用
build_application() {
    log_info "构建应用..."

    local profile="dev"
    case $DEPLOY_ENV in
        "test")
            profile="test"
            ;;
        "prod")
            profile="prod"
            ;;
    esac

    if [[ $SKIP_TESTS == true ]]; then
        mvn clean package -DskipTests -P$profile
    else
        mvn clean package -P$profile
    fi

    # 检查构建结果
    if [[ -f "target/skytrust-platform-1.0.0-SNAPSHOT.jar" ]]; then
        log_success "应用构建完成"
        ls -lh target/*.jar
    else
        log_error "构建失败，未找到JAR文件"
        exit 1
    fi
}

# 数据库迁移
run_db_migration() {
    log_info "运行数据库迁移..."

    # 这里可以添加Flyway或Liquibase迁移命令
    # 示例: mvn flyway:migrate -P$DEPLOY_ENV

    log_warning "数据库迁移功能待实现"
    # TODO: 集成数据库迁移工具
}

# Docker部署
docker_deploy() {
    log_info "使用Docker部署到 $DEPLOY_ENV 环境..."

    case $DEPLOY_ENV in
        "dev")
            log_info "启动开发环境..."
            docker-compose up -d mysql redis rabbitmq minio mqtt ganache ai-service
            sleep 10  # 等待依赖服务启动
            docker-compose up -d skytrust-app
            ;;
        "test")
            log_info "启动测试环境..."
            docker-compose -f docker-compose.test.yml up -d
            ;;
        "prod")
            log_info "启动生产环境..."
            # 构建生产镜像
            docker build -t skytrust-platform:prod -f Dockerfile.prod .

            # 使用生产环境配置
            docker-compose -f docker-compose.prod.yml up -d

            # 检查服务状态
            sleep 30
            docker-compose -f docker-compose.prod.yml ps
            ;;
    esac

    # 显示部署信息
    log_info "显示服务状态..."
    docker-compose ps

    log_success "Docker部署完成"
    log_info "应用访问地址: http://localhost:8080"
    log_info "API文档: http://localhost:8080/api/doc.html"
}

# Kubernetes部署
k8s_deploy() {
    log_info "使用Kubernetes部署到 $DEPLOY_ENV 环境..."

    # 检查kubectl配置
    kubectl cluster-info

    # 创建命名空间
    kubectl create namespace skytrust-$DEPLOY_ENV --dry-run=client -o yaml | kubectl apply -f -

    # 部署配置
    kubectl apply -f k8s/configmap-$DEPLOY_ENV.yaml -n skytrust-$DEPLOY_ENV
    kubectl apply -f k8s/secrets-$DEPLOY_ENV.yaml -n skytrust-$DEPLOY_ENV

    # 部署数据库
    kubectl apply -f k8s/mysql-deployment.yaml -n skytrust-$DEPLOY_ENV
    kubectl apply -f k8s/redis-deployment.yaml -n skytrust-$DEPLOY_ENV

    # 等待数据库就绪
    kubectl wait --for=condition=ready pod -l app=mysql -n skytrust-$DEPLOY_ENV --timeout=300s
    kubectl wait --for=condition=ready pod -l app=redis -n skytrust-$DEPLOY_ENV --timeout=300s

    # 部署应用
    kubectl apply -f k8s/app-deployment-$DEPLOY_ENV.yaml -n skytrust-$DEPLOY_ENV

    # 部署服务
    kubectl apply -f k8s/app-service.yaml -n skytrust-$DEPLOY_ENV

    # 部署Ingress（如果有）
    if [[ -f "k8s/ingress-$DEPLOY_ENV.yaml" ]]; then
        kubectl apply -f k8s/ingress-$DEPLOY_ENV.yaml -n skytrust-$DEPLOY_ENV
    fi

    # 显示部署状态
    kubectl get all -n skytrust-$DEPLOY_ENV

    log_success "Kubernetes部署完成"

    # 获取访问地址
    if [[ $DEPLOY_ENV == "prod" ]]; then
        local ingress_host=$(kubectl get ingress skytrust-ingress -n skytrust-prod -o jsonpath='{.spec.rules[0].host}')
        log_info "生产环境访问地址: https://$ingress_host"
    else
        local node_port=$(kubectl get svc skytrust-service -n skytrust-$DEPLOY_ENV -o jsonpath='{.spec.ports[0].nodePort}')
        log_info "测试环境访问地址: http://<node-ip>:$node_port"
    fi
}

# 健康检查
health_check() {
    log_info "执行健康检查..."

    local max_attempts=30
    local attempt=1

    while [[ $attempt -le $max_attempts ]]; do
        if curl -f http://localhost:8080/api/actuator/health >/dev/null 2>&1; then
            log_success "应用健康检查通过"
            return 0
        fi

        log_info "等待应用启动... ($attempt/$max_attempts)"
        sleep 5
        ((attempt++))
    done

    log_error "应用健康检查失败"
    return 1
}

# 主函数
main() {
    # 默认值
    DEPLOY_ENV="dev"
    CLEAN_BUILD=false
    BUILD_APP=false
    USE_DOCKER=false
    USE_K8S=false
    RUN_TESTS=false
    SKIP_TESTS=false
    DB_MIGRATE=false

    # 解析命令行参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_help
                exit 0
                ;;
            -e|--env)
                DEPLOY_ENV="$2"
                shift 2
                ;;
            -c|--clean)
                CLEAN_BUILD=true
                shift
                ;;
            -b|--build)
                BUILD_APP=true
                shift
                ;;
            -d|--docker)
                USE_DOCKER=true
                shift
                ;;
            -k|--k8s)
                USE_K8S=true
                shift
                ;;
            -t|--test)
                RUN_TESTS=true
                shift
                ;;
            --skip-tests)
                SKIP_TESTS=true
                shift
                ;;
            --db-migrate)
                DB_MIGRATE=true
                shift
                ;;
            *)
                log_error "未知选项: $1"
                show_help
                exit 1
                ;;
        esac
    done

    # 显示部署信息
    log_info "开始部署 SkyTrust 平台"
    log_info "部署环境: $DEPLOY_ENV"
    log_info "时间: $(date)"

    # 环境检查
    check_environment

    # 清理构建
    if [[ $CLEAN_BUILD == true ]]; then
        clean_build
    fi

    # 运行测试
    if [[ $RUN_TESTS == true ]]; then
        run_tests
    fi

    # 数据库迁移
    if [[ $DB_MIGRATE == true ]]; then
        run_db_migration
    fi

    # 构建应用
    if [[ $BUILD_APP == true ]]; then
        build_application
    fi

    # Docker部署
    if [[ $USE_DOCKER == true ]]; then
        docker_deploy
        health_check
    fi

    # Kubernetes部署
    if [[ $USE_K8S == true ]]; then
        k8s_deploy
    fi

    # 如果没有指定任何部署操作，显示帮助
    if [[ $CLEAN_BUILD == false ]] && [[ $BUILD_APP == false ]] && [[ $USE_DOCKER == false ]] && [[ $USE_K8S == false ]]; then
        log_warning "未指定部署操作"
        show_help
    fi

    log_success "部署流程完成"
    log_info "详细日志请查看: logs/deploy-$(date +%Y%m%d-%H%M%S).log"
}

# 执行主函数
main "$@"